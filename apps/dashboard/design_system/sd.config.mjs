import StyleDictionary from 'style-dictionary';

const sd = new StyleDictionary({
  source: ['design_system/tokens/**/*.json'], 
  platforms: {
    css: {
      transformGroup: 'css',
      buildPath: 'app/', //builds tokens.css and outputs to app folder
      files: [{
        destination: 'tokens.css',
        format: 'css/variables'
      }]
    }
  }
});

await sd.buildAllPlatforms();

