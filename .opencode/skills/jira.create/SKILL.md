/ji---
name: jira.create
description: Use when creating a Jira ticket for Fleet project - when user says "创建 jira"、"新建 ticket"、"jira create"、"开 ticket"、"/jira.create", or provides a bug/feature description needing tracking. Reads relevant code, drafts ticket in English, confirms with user, then creates via Atlassian MCP.
---

# jira.create

## Overview

Standardized workflow for creating Fleet Jira tickets: read code → draft in English → confirm with user → create via Atlassian MCP.

**Invocation:** `/jira.create <description or file path>`

---

## Step 1: Understand Context

User input may come in two forms — handle both:

**A. File path provided** (e.g. `/jira.create repository/rental.go 计算错误`)
- Use Read to open the file; locate the relevant function/logic
- Extract: symptom, affected method, scope of impact

**B. Background description only** (e.g. "遇到了 XXX 问题，需要开 ticket")
- Extract keywords from the description (module name, feature area, error type)
- Use Grep/Glob to search the codebase for relevant files and functions
- Read the located code to understand the existing implementation
- Identify: which file/method is involved, what the current behavior is, what the expected behavior should be

In both cases, **do not skip code exploration**. The ticket description must reference specific files and methods found in the actual codebase.

---

## Step 2: Draft Ticket (All English)

Generate the following fields:

| Field | Guidance |
|-------|----------|
| `summary` | Concise one-line title in English |
| `description` | Problem description + affected files/methods |
| `issueType` | `Bug` / `Story` / `Task` (infer from context) |
| `priority` | P0–P3 (infer from impact; ask if unclear) |

**Priority guide:**
- **P0**: System down / data loss / critical blocker
- **P1**: Major feature broken, no workaround
- **P2**: Feature degraded, workaround exists
- **P3**: Minor issue, cosmetic, or low impact

---

## Step 3: Show Draft & Confirm

Present draft as a table:

```
| Field       | Value                          |
|-------------|-------------------------------|
| Summary     | [summary]                     |
| Type        | Bug / Story / Task            |
| Priority    | P0 / P1 / P2 / P3             |
| Description | [first 2 lines...]            |
```

Ask user:
1. Confirm and create
2. Modify a field
3. Cancel

---

## Step 4: Get Current User

Call `mcp__claude_ai_Atlassian__atlassianUserInfo` to get `account_id` for assignee.

---

## Step 5: Create Ticket

Call `mcp__claude_ai_Atlassian__createJiraIssue` **without** `customfield_23160` in additional_fields — that field cannot be set at creation time:

```json
{
  "cloudId": "14c1d2a0-1b35-43da-b7ac-4b47ffcc4f4a",
  "projectKey": "FLEET",
  "summary": "<summary>",
  "issueTypeName": "<Epic|Bug|Story|Task>",
  "description": "<description>",
  "assignee_account_id": "<from step 4>",
  "contentFormat": "markdown"
}
```

---

## Step 6: Set Priority (post-creation)

After the ticket is created, call `mcp__claude_ai_Atlassian__editJiraIssue` to set priority:

```json
{
  "cloudId": "14c1d2a0-1b35-43da-b7ac-4b47ffcc4f4a",
  "issueIdOrKey": "<FLEET-XXX>",
  "fields": {
    "customfield_23160": { "id": "<priority_id>" }
  }
}
```

**Priority ID mapping:**

| Priority | ID    |
|----------|-------|
| P0       | 61625 |
| P1       | 61626 |
| P2       | 61627 |
| P3       | 61628 |

If the edit fails with "not on the appropriate screen", skip and note to user that priority must be set manually in Jira.

### Sprint assignment

**Must be set manually.** Sprint cannot be set via the Atlassian MCP tools — both `customfield_10020` in `createJiraIssue` and `editJiraIssue` return "not on the appropriate screen". Setting sprint requires the Jira Agile REST API (`/rest/agile/1.0/sprint/{sprintId}/issue`) which is not exposed by the MCP.

After creating the ticket, tell the user: "Please assign this ticket to the current Fleet sprint manually in Jira."

---

## Step 7: Link to Epic (if applicable)

If the user specified an Epic to link this ticket to, use `mcp__claude_ai_Atlassian__editJiraIssue` with the `parent` field — this is the correct way to assign an Epic in team-managed Jira projects. Do NOT use `customfield_10014` or `createIssueLink`:

```json
{
  "cloudId": "14c1d2a0-1b35-43da-b7ac-4b47ffcc4f4a",
  "issueIdOrKey": "<FLEET-XXX (this ticket)>",
  "fields": {
    "parent": { "key": "<FLEET-YYY (Epic)>" }
  }
}
```

---

On success, output the ticket key and link:
`FLEET-XXX → https://go-jek.atlassian.net/browse/FLEET-XXX`

---

## Constraints

- Summary and Description **must be in English**
- Priority **must be explicitly set** (never omit) — set via `editJiraIssue` after creation
- Sprint **cannot be set via API** — remind user to assign to current Fleet sprint manually in Jira
- Assignee **always = current logged-in user** (via `atlassianUserInfo`)
- `cloudId` is fixed: `14c1d2a0-1b35-43da-b7ac-4b47ffcc4f4a`
- Epic linking **must use `editJiraIssue` with `parent` field**, never `customfield_10014` or `createIssueLink`