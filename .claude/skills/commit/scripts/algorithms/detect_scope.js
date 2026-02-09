#!/usr/bin/env node
/**
 * detect_scope.js
 * Purpose: Detect optimal scope from changed files
 * Usage: node detect_scope.js < input.json
 * Input: {"files": ["path1", "path2", ...]}
 * Output: {"scope": "detected-scope", "alternatives": [...]}
 */

const path = require('path');

function findCommonDirectory(files) {
  if (files.length === 1) {
    return path.dirname(files[0]);
  }

  const parts = files.map(f => f.split('/'));
  const minLength = Math.min(...parts.map(p => p.length));

  let commonPath = [];
  for (let i = 0; i < minLength - 1; i++) {
    const part = parts[0][i];
    if (parts.every(p => p[i] === part)) {
      commonPath.push(part);
    } else {
      break;
    }
  }

  return commonPath.join('/') || '.';
}

function detectModuleName(commonDir, files) {
  // Pattern matching for known module structures
  const patterns = {
    '.claude/skills/commit': 'commit-skill',
    'spring-batch': 'spring-batch',
    'spring-security': 'spring-security',
    'spring-kafka': 'spring-kafka',
    'spring-jpa': 'spring-jpa'
  };

  // Check if commonDir matches any pattern
  for (const [pattern, moduleName] of Object.entries(patterns)) {
    if (commonDir.includes(pattern)) {
      return moduleName;
    }
  }

  // Extract module name from common directory
  const parts = commonDir.split('/').filter(p => p);

  // Skip common prefixes
  const skipPrefixes = ['src', 'main', 'java', 'test', 'resources'];
  const relevantParts = parts.filter(p => !skipPrefixes.includes(p));

  if (relevantParts.length > 0) {
    return relevantParts[relevantParts.length - 1];
  }

  return path.basename(commonDir) || 'root';
}

function detectOptimalScope(files) {
  // Single file - use filename
  if (files.length === 1) {
    return {
      scope: path.basename(files[0]),
      type: 'file',
      confidence: 'high'
    };
  }

  // Find common directory
  const commonDir = findCommonDirectory(files);

  // Try to detect module name
  const moduleName = detectModuleName(commonDir, files);

  if (moduleName !== 'root') {
    return {
      scope: moduleName,
      type: 'module',
      confidence: 'high'
    };
  }

  // Fallback to directory name
  const dirName = path.basename(commonDir);
  return {
    scope: dirName || 'multiple',
    type: 'directory',
    confidence: 'medium'
  };
}

function generateAlternativeScopes(files, primaryScope) {
  const alternatives = [];

  // Add filename alternatives (first 3 files)
  files.slice(0, 3).forEach(file => {
    const filename = path.basename(file);
    if (filename !== primaryScope.scope) {
      alternatives.push({
        scope: filename,
        type: 'file',
        confidence: 'low'
      });
    }
  });

  // Add parent directory alternative
  if (files.length > 0) {
    const commonDir = findCommonDirectory(files);
    const parentDir = path.dirname(commonDir);
    const parentName = path.basename(parentDir);

    if (parentName && parentName !== '.' && parentName !== primaryScope.scope) {
      alternatives.push({
        scope: parentName,
        type: 'parent-directory',
        confidence: 'medium'
      });
    }
  }

  return alternatives.slice(0, 4); // Max 4 alternatives
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

      const primaryScope = detectOptimalScope(input.files);
      const alternatives = generateAlternativeScopes(input.files, primaryScope);

      const output = {
        primary: primaryScope,
        alternatives: alternatives,
        fileCount: input.files.length
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

module.exports = { detectOptimalScope, generateAlternativeScopes };
