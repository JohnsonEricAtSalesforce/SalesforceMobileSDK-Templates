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

| Skill | Description |
|---|---|
| `create-ios-app-with-mobile-sdk/` | Create a new iOS Swift app from scratch with Mobile SDK already integrated |
| `add-mobile-sdk-ios/` | Add Mobile SDK authentication to an existing iOS Swift app |
| `add-smartstore-ios/` | Add SmartStore (encrypted local database) to an iOS Swift app that already has Mobile SDK |
| `add-mobilesync-ios/` | Add MobileSync (cloud data sync) to an iOS Swift app that already has SmartStore |

### SDK developer skills (internal)

Skills intended for **developers working on Mobile SDK itself** (contributors, maintainers). These are hidden from the default skill listing and only loaded by Claude Code when the repo is open locally — the `npx skills` CLI is not the delivery mechanism.

They use the same directory/`SKILL.md` format as consumer skills, but include `metadata.internal: true` in their frontmatter:

```
.claude/skills/
  my-sdk-skill/
    SKILL.md
```

| Skill | Description |
|---|---|
| `remove-template/` | Remove a template from this repository |
| `test-template/` | Test templates with `test_template.sh` |
| `update-ios-deployment-target/` | Bump the minimum iOS deployment target across all templates |

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

## Testing a Consumer Skill

Before shipping a new consumer skill, exercise it against a real blank app to verify it produces a working result end-to-end.

### Process

1. **Create a blank app** using `xcodegen` (or Xcode's new project wizard) in a temp directory
2. **Apply the skill** — run Claude Code in that directory and invoke the skill
3. **Build** — `xcodebuild` against a simulator to confirm no compile errors
4. **Run on simulator** — verify the skill's core behaviour (e.g. login screen appears, SDK initializes)
5. **Clean up** — delete the temp directory after validation

### Example: testing `add-mobile-sdk-ios`

```bash
# Create blank app
mkdir /tmp/SkillTest && cd /tmp/SkillTest
# ... write project.yml, generate with xcodegen ...

# Apply the skill (Claude Code invokes it)
# Then build:
xcodebuild -workspace SkillTest.xcworkspace \
  -scheme SkillTest \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' \
  build
```

Expected: `** BUILD SUCCEEDED **` and Salesforce login screen on first run.

### What to check

- [ ] Project builds without errors
- [ ] Login screen appears on first launch
- [ ] Post-login screen is reachable
- [ ] No crash on cold start

---

**SDK developer skill** — create a subdirectory with a `SKILL.md` that includes `metadata.internal: true`:

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
