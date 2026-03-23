# Changelog

All notable changes to the docs agent consolidation project.

## [2.0.0] - 2026-03-21

### Major Changes

#### Agent Consolidation
- **Consolidated 11 agents into 3 unified agents**
  - Before: 8 agents in `blog/` + 3 agents in `docs/` = 11 agents
  - After: 3 unified agents in `docs/`

#### New Unified Agents

| Agent | Modes/Actions | Based On |
|-------|---------------|----------|
| `reviewer` | standard, technical, critical, reader, structure, comprehensive | doc-reviewer, tech-writer, critic, reader-advocate, content-strategist |
| `writer` | draft, revise | doc-writer |
| `translator` | translate (with settings.json integration) | translator |

#### Settings Integration
- Added `settings.json` `language` key integration
- `language: korean` enables Korean validation and translation
- Automatic language detection for documents

### Features

#### Reviewer Agent
- 5 verification modes + comprehensive mode
- Mode selection via `mode` parameter
- Korean language validation (honorifics, speech level)
- Unified checklist output format
- Severity-based prioritization (CRITICAL/WARNING/INFO)

#### Writer Agent
- Draft creation mode
- Feedback-based revision mode
- Original file protection (no direct modification)
- Temporary workspace in `${CLAUDE_TMP_DIR}`

#### Translator Agent
- English to Korean translation
- Settings.json language integration
- File naming convention (`01-section-01-title.md`)
- Source attribution in header

### Skill Configuration

#### New Workflow Files
- `blog-review.yaml`: Multi-mode blog content verification
- `korean-doc.yaml`: Korean document comprehensive validation
- `write-validate.yaml`: Write-validate feedback loop
- `quick-check.yaml`: Quick quality check (60s timeout)

#### Config.yaml
- Agent mapping for 3 unified agents
- Workflow definitions
- Result aggregation settings
- Error handling configuration

### Project Structure

```
.claude/
├── agents/docs/
│   ├── reviewer.md          # NEW: Unified verification agent
│   ├── writer.md            # NEW: Writing/improvement agent
│   └── translator.md        # UPDATED: With settings integration
├── skills/docs/
│   ├── SKILL.md             # UPDATED: v2.0 documentation
│   ├── config.yaml          # NEW: Agent mapping and workflows
│   └── workflows/
│       ├── blog-review.yaml         # NEW
│       ├── korean-doc.yaml          # NEW
│       ├── write-validate.yaml      # NEW
│       └── quick-check.yaml         # NEW
└── docs/plans/
    ├── agent-consolidation-plan.md  # Project plan
    ├── phase0-report.md             # Phase 0 report
    ├── phase4-test-report.md        # Phase 4 test report
    └── CHANGELOG.md                 # This file
```

### Breaking Changes

- Legacy agents in `blog/` are deprecated but preserved
- New orchestration requires `mode` parameter for reviewer
- Output paths changed to `${CLAUDE_TMP_DIR}/`

### Migration Guide

#### Before (v1.x)
```yaml
# Individual agent calls
- agent: tech-writer
- agent: critic
- agent: reader-advocate
```

#### After (v2.0)
```yaml
# Mode-based reviewer calls
- agent: reviewer
  mode: technical
- agent: reviewer
  mode: critical
- agent: reviewer
  mode: reader
```

### Performance Improvements

- Reduced agent count: 11 → 3 (73% reduction)
- Simplified orchestration: agent selection → mode selection
- Unified output format across all verification modes

### Known Issues

None reported.

### Future Enhancements

- [ ] Accessibility mode for reviewer
- [ ] Batch processing for multiple documents
- [ ] Integration with external documentation platforms

---

## [1.0.0] - Legacy (Pre-consolidation)

### Original Agents

#### Blog Agents (8)
- diagram-designer
- synthesizer
- moderator
- reader-advocate
- editor
- critic
- content-strategist
- tech-writer

#### Docs Agents (3)
- doc-reviewer
- doc-writer
- translator

---

## Version History

| Version | Date | Description |
|---------|------|-------------|
| 2.0.0 | 2026-03-21 | Agent consolidation, unified agents, settings integration |
| 1.0.0 | ~2024 | Original separate agents |
