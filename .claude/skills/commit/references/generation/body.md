# Stage 2: Body Item Generation

### Core Principle

**Body purpose:**
- ❌ List changed files (already shown in git log)
- ✅ Describe what work was done

**File list vs Body:**
- What git log shows automatically: file list, changed line counts
- What Body should provide: work description, purpose, context

### Generate 10-15 Body Item Candidates (Feature-based)

**Strategy: Feature/work-centric (exclude filenames)**

```javascript
function generateBodyItems(files, diff) {
  // 1. Analyze changes and group by feature/purpose
  const features = analyzeFeatures(files, diff);

  // 2. Generate feature-based items
  const items = features.map(feature => ({
    label: feature.description,        // Work description (no filenames)
    description: feature.details,      // Detailed explanation
    score: calculateScore(feature),    // Importance score (0-100)
    relatedFiles: feature.files        // For reference (optional)
  }));

  // 3. Sort by score (high to low)
  return items.sort((a, b) => b.score - a.score);
}

function calculateScore(feature) {
  let score = 0;

  // Changed line count (40 points)
  const totalLines = feature.files.reduce((sum, f) =>
    sum + f.additions + f.deletions, 0);
  score += Math.min(totalLines / 10, 40);

  // File importance (30 points)
  const importanceScore = feature.files.reduce((sum, f) => {
    if (f.path.includes('src/main')) return sum + 15;
    if (f.path.includes('config')) return sum + 10;
    if (f.path.includes('src/test')) return sum + 5;
    return sum + 3;
  }, 0);
  score += Math.min(importanceScore, 30);

  // Commit type relevance (30 points)
  const typeRelevance = analyzeTypeRelevance(feature, detectedType);
  score += typeRelevance;

  return Math.round(score);
}
```

See [3-2-body-selection.md](../../templates/3-2-body-selection.md) for detailed algorithm.

**Example candidates:**
```
[95⭐] Change commit message generation to 3-stage selection
[90⭐] Rewrite header generation strategy to 5 options
[85⭐] Implement body item pagination
[80] Add 3 new templates (header, body, footer selection)
[75] Update core prompt to header selection approach
```

### Pagination (Per-page Display)

**Implementation:**
- Generate 10-15 candidates in Step 1 (metadata)
- Show 3 items per page in Step 2
- User can navigate: [Next Page], [Previous Page]
- Selections accumulate across pages
- Last page shows: "No body", "[Complete Selection]"

```javascript
// Step 1: Pre-generate all candidates
metadata.analysis.bodyItemCandidates = generateBodyItems(files, diff);

// Step 2: Paginate (3 items per page)
const itemsPerPage = 3;
for (let page = 0; page < totalPages; page++) {
  const start = page * itemsPerPage;
  const pageItems = candidates.slice(start, start + itemsPerPage);
  // Show pageItems with navigation options
}
```

---
