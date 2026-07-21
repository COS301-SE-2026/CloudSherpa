import Spacings from "@/design-system/layout-and-spacing/components/spacings";
import Breakpoints from "@/design-system/layout-and-spacing/components/breakpoints";
import rawTokens from "@/app/tokens/docs/design-tokens.json";

export default function LayoutAndSpacing() {
    const { spacing } = rawTokens;
    const { breakpoints } = rawTokens;

    return (
        <div>
            <Spacings spacings={spacing} />
            <Breakpoints breakpoints={breakpoints} />
        </div>
    );
}
