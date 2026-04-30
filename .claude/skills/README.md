# Mobile SDK Skills

This directory contains agent skills for [Claude Code](https://code.claude.com/docs/en/skills) and other compatible AI coding agents.

Skills are installable via the [vercel-labs/skills](https://github.com/vercel-labs/skills) CLI:

```bash
npx skills add forcedotcom/SalesforceMobileSDK-Templates
```

## Two Audiences, One Directory

Skills are split into two groups using the `metadata.internal` flag.

### Consumer skills (default)

Skills intended for **developers building apps with Mobile SDK** (SDK consumers). These are visible by default when installing via the CLI above.

Use the directory/`SKILL.md` format so the `npx skills` CLI can discover and install them:

```
.claude/skills/
  my-consumer-skill/
    SKILL.md
```

_No consumer skills yet — add them here._

### SDK developer skills (internal)

Skills intended for **developers working on Mobile SDK itself** (contributors, maintainers). These are hidden from the default skill listing and only loaded by Claude Code when the repo is open locally — the `npx skills` CLI is not the delivery mechanism.

Because they are only ever used via local repo clone, a flat `.md` file is sufficient (no need for a subdirectory):

```
.claude/skills/
  my-sdk-skill.md
```

| Skill | Description |
|---|---|
| `remove-template.md` | Remove a template from this repository |
| `test-template.md` | Test templates with `test_template.sh` |
| `update-ios-deployment-target.md` | Bump the minimum iOS deployment target across all templates |

## Adding a New Skill

**Consumer skill** — create a subdirectory with a `SKILL.md`:

```markdown
---
name: my-skill
description: What this skill does and when to use it
---

# My Skill
...
```

**SDK developer skill** — create a flat `.md` file with `metadata.internal: true`:

```markdown
---
name: my-skill
description: What this skill does and when to use it
metadata:
  internal: true
---

# My Skill
...
```
