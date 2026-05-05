module.exports = {
  ci: {
    collect: {
      startServerCommand: "npx next start",
      url: ["http://localhost:3000"],
      numberOfRuns: 3,
    },
    assert: {
      // preset: "lighthouse:recommended",
      assertions: {
        'categories:performance': ['error', { minScore: 0.8 }],
        'categories:accessibility': ['error', { minScore: 0.9 }],
      },
    },
    upload: {
      target: "temporary-public-storage",
    },
  },
};