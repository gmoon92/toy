# Stage 2: Body Item Generation

### Core Principle

**Body의 목적:**
- ❌ 변경된 파일 나열 (git log에 이미 표시됨)
- ✅ 무엇을 했는지 작업 내용 설명

**파일 리스트 vs Body:**
- Git log가 자동으로 보여주는 것: 파일 리스트, 변경 라인 수
- Body가 제공해야 하는 것: 작업 내용, 목적, 맥락

### Generate 10-15 Body Item Candidates (Feature-based)

**Strategy: Feature/작업 중심 (파일명 제외)**

```javascript
function generateBodyItems(files, diff) {
  // 1. Analyze changes and group by feature/purpose
  const features = analyzeFeatures(files, diff);

  // 2. Generate feature-based items
  const items = features.map(feature => ({
    label: feature.description,        // 작업 내용 (파일명 X)
    description: feature.details,      // 상세 설명
    score: calculateScore(feature),    // 중요도 점수 (0-100)
    relatedFiles: feature.files        // 참고용 (optional)
  }));

  // 3. Sort by score (high to low)
  return items.sort((a, b) => b.score - a.score);
}

function calculateScore(feature) {
  let score = 0;

  // 변경 라인 수 (40점)
  const totalLines = feature.files.reduce((sum, f) =>
    sum + f.additions + f.deletions, 0);
  score += Math.min(totalLines / 10, 40);

  // 파일 중요도 (30점)
  const importanceScore = feature.files.reduce((sum, f) => {
    if (f.path.includes('src/main')) return sum + 15;
    if (f.path.includes('config')) return sum + 10;
    if (f.path.includes('src/test')) return sum + 5;
    return sum + 3;
  }, 0);
  score += Math.min(importanceScore, 30);

  // 커밋 타입 관련성 (30점)
  const typeRelevance = analyzeTypeRelevance(feature, detectedType);
  score += typeRelevance;

  return Math.round(score);
}
```

See [3-2-body-selection.md](../assets/templates/3-2-body-selection.md) for detailed algorithm.

**Example candidates:**
```
[95⭐] 커밋 메시지 생성 방식을 3단계 선택으로 변경
[90⭐] 헤더 5개 생성 전략 알고리즘 재작성
[85⭐] 바디 항목 페이지네이션 구현
[80] 3개 새 템플릿 추가 (header, body, footer selection)
[75] 코어 프롬프트를 헤더 선택 방식으로 업데이트
```

### Pagination (페이지별 노출)

**Implementation:**
- Generate 10-15 candidates in Step 1 (metadata)
- Show 3 items per page in Step 2
- User can navigate: [다음 페이지], [이전 페이지]
- Selections accumulate across pages
- Last page shows: "바디 없음", "[선택 완료]"

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

