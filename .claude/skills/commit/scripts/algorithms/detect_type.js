#!/usr/bin/env node
/**
 * detect_type.js
 * Purpose: Detect commit type from changed files and diff
 * Usage: node detect_type.js < input.json
 * Input: {"files": [...], "diff": "..."}
 * Output: {"type": "feat|fix|refactor|...", "confidence": "high|medium|low"}
 */

function detectPrimaryType(files, diff = '') {
  // Test files only
  if (files.every(f => f.includes('test') || f.includes('Test'))) {
    return { type: 'test', confidence: 'high' };
  }

  // Documentation files only
  if (files.every(f => f.endsWith('.md') || f.includes('docs/'))) {
    return { type: 'docs', confidence: 'high' };
  }

  // Style files only (CSS, SCSS, etc.)
  if (files.every(f => /\.(css|scss|sass|less)$/.test(f))) {
    return { type: 'style', confidence: 'high' };
  }

  // Analyze diff content
  if (diff) {
    if (hasBugFixPatterns(diff)) {
      return { type: 'fix', confidence: 'high' };
    }
    if (hasNewFeaturePatterns(diff)) {
      return { type: 'feat', confidence: 'medium' };
    }
    if (hasRefactoringPatterns(diff)) {
      return { type: 'refactor', confidence: 'medium' };
    }
  }

  // Check file patterns
  if (files.some(f => f.includes('config') || f.includes('Config'))) {
    return { type: 'chore', confidence: 'low' };
  }

  // Default to most conservative type
  return { type: 'chore', confidence: 'low' };
}

function hasBugFixPatterns(diff) {
  const patterns = [
    /fix\s+(bug|error|issue)/i,
    /resolve\s+#\d+/i,
    /patch\s+/i,
    /\bfix\b.*\bbug\b/i,
    /correct\s+/i
  ];

  return patterns.some(p => p.test(diff));
}

function hasNewFeaturePatterns(diff) {
  const patterns = [
    /\badd\s+new\s+/i,
    /implement\s+/i,
    /introduce\s+/i,
    /create\s+new\s+/i,
    /\bfeat\b/i
  ];

  return patterns.some(p => p.test(diff));
}

function hasRefactoringPatterns(diff) {
  const patterns = [
    /refactor/i,
    /restructure/i,
    /reorganize/i,
    /rename\s+/i,
    /move\s+/i,
    /extract\s+/i
  ];

  return patterns.some(p => p.test(diff));
}

function generateAlternativeTypes(files, diff, primaryType) {
  const allTypes = ['feat', 'fix', 'refactor', 'test', 'docs', 'style', 'chore'];
  const alternatives = [];

  // Add plausible alternatives based on context
  for (const type of allTypes) {
    if (type !== primaryType.type) {
      const confidence = calculateTypeConfidence(type, files, diff);
      if (confidence !== 'none') {
        alternatives.push({ type, confidence });
      }
    }
  }

  // Sort by confidence
  const confidenceOrder = { high: 3, medium: 2, low: 1 };
  alternatives.sort((a, b) => confidenceOrder[b.confidence] - confidenceOrder[a.confidence]);

  return alternatives.slice(0, 3); // Max 3 alternatives
}

function calculateTypeConfidence(type, files, diff) {
  // Simple heuristics for alternative types
  if (type === 'docs' && files.some(f => f.endsWith('.md'))) {
    return 'medium';
  }
  if (type === 'test' && files.some(f => f.includes('test'))) {
    return 'medium';
  }
  if (type === 'refactor' && diff.includes('refactor')) {
    return 'medium';
  }
  if (type === 'feat' && files.length > 3) {
    return 'low';
  }

  return 'none';
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

      const primaryType = detectPrimaryType(input.files, input.diff || '');
      const alternatives = generateAlternativeTypes(input.files, input.diff || '', primaryType);

      const output = {
        primary: primaryType,
        alternatives: alternatives
      };

      console.log(JSON.stringify(output, null, 2));
    } catch (error) {
      console.error(JSON.stringify({
        error: error.message,
        status: 'failed'
      }));
      process.exit(1);
    }
  });
}

module.exports = { detectPrimaryType, generateAlternativeTypes };
