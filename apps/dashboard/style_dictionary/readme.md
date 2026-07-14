**setup of style dictionairy**

**basics**

Style dictionairy is a tool used to decouple our styles from the globals.css meaning it is decoupled from shadcn and tailwind.css by
allowing us to specify our brand specific styles in seperate structured .json files.

**how is works**
it takes the json files provided and compiles them into a file called tokens.css Note: the name for this was set in the config.mjs file.
During compilation it looks at the files, checks the contents, and compiles the json block differently depending on what "$type" was set. 
for example **$type:color** will be compiled to **--color-example: #39848** in the generated tokens.css file.

**types in use:**
**color** - used for specifying colors
**dimensions** - used for specifying sizes like spacing, margins, font size etc.
**number** - used for specifying numbers that can be used for multiples of another dimension. for example line height is calculated based on font size where it takes the font size (ie. 48px and mutliplies it with the multiple for tight line height which is 1.25 so we get **48 * 1.25 = 62.5 ** )
**fontWeight** - used for specifying font weights
**fontFamily** - specifys specific fonts (ie. Geist)
