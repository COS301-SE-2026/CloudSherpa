import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";

const TypographyPage = () => {
  const fontSizes = [
    { label: "XS", size: "text-xs", variable: "--font-size-xs" },
    { label: "SM", size: "text-sm", variable: "--font-size-sm" },
    { label: "Base", size: "text-base", variable: "--font-size-base" },
    { label: "LG", size: "text-lg", variable: "--font-size-lg" },
    { label: "XL", size: "text-xl", variable: "--font-size-xl" },
    { label: "2XL", size: "text-2xl", variable: "--font-size-2xl" },
    { label: "3XL", size: "text-3xl", variable: "--font-size-3xl" },
  ];

  const weights = [
    { label: "Normal", weight: "font-normal", variable: "--font-weight-normal" },
    { label: "Medium", weight: "font-medium", variable: "--font-weight-medium" },
    { label: "Semibold", weight: "font-semibold", variable: "--font-weight-semibold" },
    { label: "Bold", weight: "font-bold", variable: "--font-weight-bold" },
  ];

  return (
    <div className="flex flex-col gap-8 pb-12">
      <h1 className="text-4xl font-bold tracking-tight">Typography</h1>

      <Card>
        <CardHeader>
          <CardTitle>Font Sizes</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-6">
          {fontSizes.map((item) => (
            <div key={item.label} className="flex items-center gap-6 border-b pb-4 last:border-0 last:pb-0">
              <div className="w-16 font-mono text-muted-foreground text-xs">{item.label}</div>
              <div className={`flex-1 ${item.size}`}>You're resource usage is abnormally high.</div>
              <div className="text-xs font-mono text-muted-foreground">{`var(${item.variable})`}</div>
            </div>
          ))}
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Font Weights</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-6">
          {weights.map((item) => (
            <div key={item.label} className="flex items-center gap-6 border-b pb-4 last:border-0 last:pb-0">
              <div className="w-16 font-mono text-muted-foreground text-xs">{item.label}</div>
              <div className={`flex-1 text-base ${item.weight}`}>You're resource usage is abnormally high.</div>
              <div className="text-xs font-mono text-muted-foreground">{`var(${item.variable})`}</div>
            </div>
          ))}
        </CardContent>
      </Card>
    </div>
  );
};

export default TypographyPage;
