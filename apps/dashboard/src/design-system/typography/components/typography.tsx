import FontSize from "@/design-system/typography/components/fontSize";
import LineHeight from "@/design-system/typography/components/lineHeight";
import FontWeight from "@/design-system/typography/components/fontWeight";
import FontFamily from "@/design-system/typography/components/fontFamily";
import rawTokens from "@/app/tokens/docs/design-tokens.json";

export default function Typography() {
    const { typography } = rawTokens;
    return (
        <div className="flex flex-col gap-12">
            <FontFamily FontFamilies={typography.family} />
            <FontSize FontSizes={typography.size} />
            <FontWeight Font_Weights={typography.weight} />
            <LineHeight LineHeights={typography.lineHeight} />
        </div>
    );
}
