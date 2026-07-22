import Spacings from "@/design-system/layout-and-spacing/components/spacings";
import Breakpoints from "@/design-system/layout-and-spacing/components/breakpoints";
import Radii from "@/design-system/layout-and-spacing/components/radii";
import Borders from "@/design-system/layout-and-spacing/components/borders";
import rawTokens from "@/app/tokens/docs/design-tokens.json";

export default function LayoutAndSpacing() {
    const { spacing } = rawTokens;
    const { breakpoints } = rawTokens;
    const { radii } = rawTokens;
    const { borders } = rawTokens;

    return (
        <div className="flex flex-col gap-12">
            <Spacings spacings={spacing} />
            <Breakpoints breakpoints={breakpoints} />
            <Radii radii={radii} />
            <Borders borders={borders} />
        </div>
    );
}
