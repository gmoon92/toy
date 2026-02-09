#!/usr/bin/env node
/**
 * generate_headers.js
 * Purpose: Generate 5 commit header messages (2 recommended + 3 general)
 * Usage: node generate_headers.js < input.json
 * Input: {"files": [...], "diff": "...", "scope": {...}, "type": {...}}
 * Output: {"recommended": [msg1, msg2], "general": [msg3, msg4, msg5]}
 */

const detectScope = require('../algorithms/detect_scope');
const detectType = require('../algorithms/detect_type');

function generateHeaderMessage(type, scope, description) {
  return `${type}(${scope}): ${description}`;
}

function generateRecommended1(analysis) {
  // Use primary scope and type
  const scope = analysis.scope?.primary?.scope || 'unknown';
  const type = analysis.type?.primary?.type || 'chore';

  // Generate clear, concise description
  const description = generatePrimaryDescription(analysis);

  return {
    message: generateHeaderMessage(type, scope, description),
    rank: 1,
    label: '추천'
  };
}

function generateRecommended2(analysis) {
  // Use alternative scope or type
  const primaryScope = analysis.scope?.primary?.scope || 'unknown';
  const primaryType = analysis.type?.primary?.type || 'chore';

  // Try alternative scope first
  let scope = primaryScope;
  let type = primaryType;

  if (analysis.scope?.alternatives?.length > 0) {
    scope = analysis.scope.alternatives[0].scope;
  } else if (analysis.type?.alternatives?.length > 0) {
    type = analysis.type.alternatives[0].type;
  }

  const description = generateAlternativeDescription(analysis, type);

  return {
    message: generateHeaderMessage(type, scope, description),
    rank: 2,
    label: '추천'
  };
}

function generateGeneral(analysis, index) {
  const scopes = [
    analysis.scope?.primary?.scope,
    ...(analysis.scope?.alternatives?.map(a => a.scope) || [])
  ].filter(Boolean);

  const types = [
    analysis.type?.primary?.type,
    ...(analysis.type?.alternatives?.map(a => a.type) || [])
  ].filter(Boolean);

  // Rotate through combinations
  const scopeIndex = index % scopes.length;
  const typeIndex = Math.floor(index / scopes.length) % types.length;

  const scope = scopes[scopeIndex] || 'unknown';
  const type = types[typeIndex] || 'chore';

  const description = generateVariationDescription(analysis, type, index);

  return {
    message: generateHeaderMessage(type, scope, description),
    rank: 3 + index,
    label: '일반'
  };
}

function generatePrimaryDescription(analysis) {
  const fileCount = analysis.files?.length || 0;
  const type = analysis.type?.primary?.type || 'chore';

  // Generate description based on type and file count
  if (type === 'docs') {
    return '문서 업데이트';
  }
  if (type === 'fix') {
    return fileCount === 1 ? '버그 수정' : '여러 버그 수정';
  }
  if (type === 'feat') {
    return fileCount === 1 ? '새 기능 추가' : '여러 기능 추가';
  }
  if (type === 'refactor') {
    return '코드 리팩토링';
  }
  if (type === 'test') {
    return '테스트 추가';
  }
  if (type === 'style') {
    return '스타일 수정';
  }

  return '코드 변경';
}

function generateAlternativeDescription(analysis, type) {
  if (type === 'docs') {
    return '문서 개선';
  }
  if (type === 'fix') {
    return '문제 해결';
  }
  if (type === 'feat') {
    return '기능 구현';
  }
  if (type === 'refactor') {
    return '구조 개선';
  }
  if (type === 'test') {
    return '테스트 보강';
  }

  return '변경사항 적용';
}

function generateVariationDescription(analysis, type, index) {
  const variations = {
    docs: ['문서 수정', '문서 정리', '문서 보완'],
    fix: ['오류 수정', '버그 패치', '문제 해결'],
    feat: ['기능 추가', '신규 기능', '기능 개발'],
    refactor: ['리팩토링', '코드 정리', '구조 변경'],
    test: ['테스트 작성', '테스트 추가', '테스트 개선'],
    style: ['스타일 변경', '포맷 수정', '스타일 개선'],
    chore: ['변경사항', '수정사항', '업데이트']
  };

  const typeVariations = variations[type] || variations.chore;
  return typeVariations[index % typeVariations.length];
}

function generateAllHeaders(analysis) {
  const recommended1 = generateRecommended1(analysis);
  const recommended2 = generateRecommended2(analysis);

  const general = [];
  for (let i = 0; i < 3; i++) {
    general.push(generateGeneral(analysis, i));
  }

  return {
    recommended: [recommended1, recommended2],
    general: general
  };
}

// Main execution
if (require.main === module) {
  let inputData = '';

  process.stdin.on('data', chunk => {
    inputData += chunk;
  });

  process.stdin.on('end', () => {
    try {
      const input = JSON.parse(inputData);

      if (!input.files || !Array.isArray(input.files)) {
        throw new Error('Input must contain "files" array');
      }

      // Use pre-analyzed scope and type if provided, otherwise analyze
      const analysis = {
        files: input.files,
        scope: input.scope || detectScope.detectOptimalScope(input.files),
        type: input.type || detectType.detectPrimaryType(input.files, input.diff || '')
      };

      const headers = generateAllHeaders(analysis);

      console.log(JSON.stringify(headers, null, 2));
    } catch (error) {
      console.error(JSON.stringify({
        error: error.message,
        status: 'failed'
      }));
      process.exit(1);
    }
  });
}

module.exports = { generateAllHeaders };
