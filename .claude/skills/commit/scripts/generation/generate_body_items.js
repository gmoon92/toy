#!/usr/bin/env node

/**
 * Generate body item candidates for commit message
 *
 * Input (stdin): JSON with { files, diff, type }
 * Output (stdout): JSON with body item candidates sorted by score
 *
 * Usage:
 *   echo '{"files":[...],"diff":"...","type":"feat"}' | node generate_body_items.js
 */

const readline = require('readline');

// Read JSON from stdin
async function readStdin() {
  const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    terminal: false
  });

  let input = '';
  for await (const line of rl) {
    input += line;
  }
  return JSON.parse(input);
}

/**
 * Calculate importance score (0-100)
 *
 * Components:
 * - Changes: 40 points (based on additions + deletions)
 * - Importance: 30 points (based on file path patterns)
 * - Type relevance: 30 points (based on commit type)
 */
function calculateScore(feature, detectedType) {
  let score = 0;

  // 1. Changes: 40 points (based on total lines changed)
  const totalLines = feature.files.reduce((sum, f) =>
    sum + (f.additions || 0) + (f.deletions || 0), 0);
  score += Math.min(totalLines / 10, 40);

  // 2. Importance: 30 points (based on file path patterns)
  const importanceScore = feature.files.reduce((sum, f) => {
    const path = f.path || '';
    if (path.includes('src/main')) return sum + 15;
    if (path.includes('config')) return sum + 10;
    if (path.includes('src/test')) return sum + 5;
    return sum + 3;
  }, 0);
  score += Math.min(importanceScore, 30);

  // 3. Type relevance: 30 points
  const typeRelevance = analyzeTypeRelevance(feature, detectedType);
  score += typeRelevance;

  return Math.round(score);
}

/**
 * Analyze type relevance (0-30 points)
 */
function analyzeTypeRelevance(feature, type) {
  const keywords = {
    feat: ['add', 'implement', 'create', 'new', '추가', '구현', '생성'],
    fix: ['fix', 'bug', 'issue', 'resolve', '수정', '해결', '버그'],
    refactor: ['refactor', 'restructure', 'improve', '리팩토링', '개선', '재구조화'],
    test: ['test', 'spec', 'coverage', '테스트', '검증'],
    docs: ['docs', 'readme', 'comment', '문서', '주석'],
    style: ['style', 'format', 'lint', '스타일', '포맷'],
    chore: ['chore', 'dependency', 'config', '설정', '의존성']
  };

  const relevantKeywords = keywords[type] || [];
  const description = feature.description.toLowerCase();

  // Check keyword matches
  const matches = relevantKeywords.filter(kw => description.includes(kw)).length;
  return Math.min(matches * 10, 30);
}

/**
 * Analyze features/work from files and diff
 * Groups changes by logical features
 */
function analyzeFeatures(files, diff) {
  const features = [];

  // Strategy 1: Group by directory/module
  const moduleGroups = groupByModule(files);

  for (const [module, moduleFiles] of Object.entries(moduleGroups)) {
    const feature = extractFeatureFromFiles(module, moduleFiles, diff);
    if (feature) {
      features.push(feature);
    }
  }

  // Strategy 2: Generate from individual significant files
  const significantFiles = files.filter(f => {
    const changes = (f.additions || 0) + (f.deletions || 0);
    return changes > 20; // Threshold for significant changes
  });

  for (const file of significantFiles) {
    const feature = extractFeatureFromFile(file, diff);
    if (feature && !isDuplicate(features, feature)) {
      features.push(feature);
    }
  }

  return features;
}

/**
 * Group files by module/directory
 */
function groupByModule(files) {
  const groups = {};

  for (const file of files) {
    const path = file.path || file;
    const parts = path.split('/');

    // Detect module name (directory before file)
    let module = 'root';
    if (parts.length > 1) {
      module = parts[parts.length - 2];
    }

    if (!groups[module]) {
      groups[module] = [];
    }
    groups[module].push(file);
  }

  return groups;
}

/**
 * Extract feature description from module and files
 */
function extractFeatureFromFiles(module, files, diff) {
  const fileNames = files.map(f => {
    const path = f.path || f;
    return path.split('/').pop();
  });

  // Detect patterns from filenames
  const patterns = detectPatterns(fileNames, diff);

  if (patterns.length === 0) {
    return null;
  }

  return {
    description: patterns[0].description,
    details: patterns[0].details,
    files: files.map(f => ({
      path: f.path || f,
      additions: f.additions || 0,
      deletions: f.deletions || 0
    })),
    module: module
  };
}

/**
 * Extract feature from single file
 */
function extractFeatureFromFile(file, diff) {
  const path = file.path || file;
  const fileName = path.split('/').pop();
  const baseName = fileName.replace(/\.(java|js|ts|py|go|rb|kt)$/, '');

  // Generate description based on file name patterns
  let description = `${baseName} 구현`;
  let details = `${fileName} 파일 수정`;

  // Enhance with diff analysis if available
  if (diff && diff.includes(path)) {
    const diffSection = extractFileDiff(diff, path);
    const enhancement = enhanceFromDiff(diffSection);
    if (enhancement) {
      description = enhancement.description || description;
      details = enhancement.details || details;
    }
  }

  return {
    description,
    details,
    files: [{
      path: path,
      additions: file.additions || 0,
      deletions: file.deletions || 0
    }],
    module: path.split('/').slice(-2, -1)[0] || 'root'
  };
}

/**
 * Detect patterns from file names
 */
function detectPatterns(fileNames, diff) {
  const patterns = [];

  // Common patterns
  const patternMap = {
    'Service': { desc: '비즈니스 로직 구현', detail: '서비스 레이어 로직' },
    'Controller': { desc: 'API 엔드포인트 구현', detail: 'REST API 컨트롤러' },
    'Repository': { desc: '데이터 접근 레이어 구현', detail: '데이터베이스 연동 코드' },
    'Config': { desc: '설정 추가', detail: '애플리케이션 설정' },
    'Test': { desc: '테스트 코드 작성', detail: '단위/통합 테스트' },
    'Util': { desc: '유틸리티 함수 추가', detail: '공통 유틸리티' },
    'DTO': { desc: 'DTO 클래스 추가', detail: '데이터 전송 객체' },
    'Entity': { desc: '엔티티 정의', detail: 'JPA 엔티티 클래스' },
    'Filter': { desc: '필터 구현', detail: 'HTTP 요청 필터' },
    'Interceptor': { desc: '인터셉터 구현', detail: 'HTTP 인터셉터' }
  };

  for (const [pattern, info] of Object.entries(patternMap)) {
    const matches = fileNames.filter(name => name.includes(pattern));
    if (matches.length > 0) {
      patterns.push({
        description: info.desc,
        details: info.detail,
        matchedFiles: matches
      });
    }
  }

  // Fallback: generic description
  if (patterns.length === 0 && fileNames.length > 0) {
    patterns.push({
      description: '코드 수정',
      details: `${fileNames.length}개 파일 변경`,
      matchedFiles: fileNames
    });
  }

  return patterns;
}

/**
 * Extract diff section for specific file
 */
function extractFileDiff(diff, filePath) {
  const lines = diff.split('\n');
  const fileStart = lines.findIndex(line => line.includes(`diff --git`) && line.includes(filePath));

  if (fileStart === -1) return '';

  const fileEnd = lines.slice(fileStart + 1).findIndex(line => line.startsWith('diff --git'));
  const endIndex = fileEnd === -1 ? lines.length : fileStart + fileEnd + 1;

  return lines.slice(fileStart, endIndex).join('\n');
}

/**
 * Enhance description from diff content
 */
function enhanceFromDiff(diffSection) {
  if (!diffSection) return null;

  // Simple heuristics based on diff content
  const addedLines = diffSection.split('\n').filter(l => l.startsWith('+')).length;
  const removedLines = diffSection.split('\n').filter(l => l.startsWith('-')).length;

  if (removedLines > addedLines * 2) {
    return { description: '코드 제거 및 정리', details: '불필요한 코드 삭제' };
  }

  if (addedLines > removedLines * 2) {
    return { description: '새로운 기능 추가', details: '기능 구현' };
  }

  return { description: '코드 수정', details: '기존 코드 개선' };
}

/**
 * Check if feature is duplicate
 */
function isDuplicate(features, newFeature) {
  return features.some(f =>
    f.description === newFeature.description &&
    f.module === newFeature.module
  );
}

/**
 * Main execution
 */
async function main() {
  try {
    const input = await readStdin();
    const { files = [], diff = '', type = 'chore' } = input;

    if (files.length === 0) {
      console.error('No files provided');
      process.exit(1);
    }

    // Analyze features
    const features = analyzeFeatures(files, diff);

    // Generate body items with scores
    const items = features.map(feature => ({
      label: feature.description,
      description: feature.details,
      score: calculateScore(feature, type),
      relatedFiles: feature.files.map(f => f.path),
      module: feature.module
    }));

    // Sort by score (high to low)
    items.sort((a, b) => b.score - a.score);

    // Limit to top 15 candidates
    const topItems = items.slice(0, 15);

    // Output
    console.log(JSON.stringify({
      items: topItems,
      totalCount: topItems.length,
      detectedType: type
    }, null, 2));

  } catch (error) {
    console.error('Error:', error.message);
    process.exit(1);
  }
}

main();
