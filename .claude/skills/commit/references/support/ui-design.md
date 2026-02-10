# Commit Message UI/UX Design

User-friendly 3-stage message composition with clear guidance and confirmation.

This directory contains individual UI templates for user interactions during the commit process, along with comprehensive UI/UX design guidelines.

---

## Template Files

Individual UI templates for each stage of the commit process:

- **[1-tidy-first.md](../../templates/1-tidy-first.md)** - Tidy First violation detection
- **[2-logical-independence.md](../../templates/2-logical-independence.md)** - Logical independence detection
- **[3-1-header-selection.md](../../templates/3-1-header-selection.md)** - Stage 1: Header message selection
- **[3-2-body-selection.md](../../templates/3-2-body-selection.md)** - Stage 2: Body item multi-select
- **[3-3-footer-selection.md](../../templates/3-3-footer-selection.md)** - Stage 3: Footer selection
- **[4-final-confirmation.md](../../templates/4-final-confirmation.md)** - Final confirmation
- **[5-direct-input.md](../../templates/5-direct-input.md)** - Message editing (direct input)

**Note:** Load templates selectively when needed for token efficiency (75-90% savings vs loading all content).

---

## Related Documents

- **[process/step3-message.md](../process/step3-message.md)** - 3-stage message composition
  - How UI templates are used in Step 3
  - Template loading sequence
- **[generation/header.md](../generation/header.md)** - Header generation for Stage 1
  - Algorithm used in header selection template
- **[generation/body.md](../generation/body.md)** - Body generation for Stage 2
  - Feature-based candidates in body selection template
- **[generation/footer.md](../generation/footer.md)** - Footer options for Stage 3
  - Footer selection template design
- **[validation/rules.md](../validation/rules.md)** - Validation rules
  - Format validation
- **[examples.md](examples.md)** - Complete examples
  - Real commit message examples
