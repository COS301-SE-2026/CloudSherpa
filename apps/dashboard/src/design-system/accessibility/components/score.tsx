import SubSectionHeading from "@/design-system/shared/components/subsectionHeading";
import { Alert, AlertDescription, AlertTitle } from "@/components/atoms/alert";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/atoms/card";
import {
    Table,
    TableHeader,
    TableRow,
    TableHead,
    TableCell,
    TableBody,
} from "@/components/atoms/table";

const testScores = [
    { page: "landing", p: 96, a: 94, b: 96, s: 100 },
    { page: "login", p: 96, a: 84, b: 96, s: 100 },
    { page: "dashboard", p: 73, a: 94, b: 100, s: 100 },
    { page: "connection manager", p: 97, a: 87, b: 100, s: 100 },
    { page: "setupwizard/step1", p: 98, a: 96, b: 100, s: 100 },
    { page: "setupwizard/step2", p: 98, a: 96, b: 100, s: 100 },
    { page: "setupwizard/step3", p: 98, a: 96, b: 100, s: 100 },
    { page: "connection details", p: 87, a: 90, b: 100, s: 100 },
    { page: "resource manager", p: 97, a: 91, b: 100, s: 100 },
];

const themeTokenContrastScores = [
    { token: "Background", darkRatio: "14.18:1", lightRatio: "14.18:1" },
    { token: "Card", darkRatio: "13.11:1", lightRatio: "14.18:1" },
    { token: "Popover", darkRatio: "13.11:1", lightRatio: "14.18:1" },
    { token: "Primary", darkRatio: "3.45:1", lightRatio: "4.82:1" },
    { token: "Secondary", darkRatio: "9.54:1", lightRatio: "11.83:1" },
    { token: "Muted", darkRatio: "5.27:1", lightRatio: "4.15:1" },
    { token: "Accent", darkRatio: "9.54:1", lightRatio: "11.83:1" },
    { token: "Destructive", darkRatio: "3.26:1", lightRatio: "4.24:1" },
    { token: "Success", darkRatio: "2.83:1", lightRatio: "1.82:1" },
    { token: "Warning", darkRatio: "2.66:1", lightRatio: "2.66:1" },
];

export default function Operable() {
    const getAverage = (key: "p" | "a" | "b" | "s") => {
        const total = testScores.reduce((sum, item) => sum + item[key], 0);
        return Math.round(total / testScores.length);
    };

    const avgPerformance = getAverage("p");
    const avgAccessibility = getAverage("a");
    const avgBestPractices = getAverage("b");
    const avgSEO = getAverage("s");

    return (
        <div className="flex flex-col gap-6">
            <SubSectionHeading
                title="Lighthouse scores"
                description="These are the averages across select pages for our lighthouse scores over 4 categories"
            />

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <Card>
                    <CardHeader>
                        <CardTitle>Performance</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <h1 className="text-2xl">{avgPerformance}</h1>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Accessibility</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <h1 className="text-2xl">{avgAccessibility}</h1>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>Best Practices</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <h1 className="text-2xl">{avgBestPractices}</h1>
                    </CardContent>
                </Card>

                <Card>
                    <CardHeader>
                        <CardTitle>SEO</CardTitle>
                    </CardHeader>
                    <CardContent className="flex flex-col gap-4">
                        <h1 className="text-2xl">{avgSEO}</h1>
                    </CardContent>
                </Card>
            </div>

            <Card>
                <CardHeader>
                    <CardTitle>Aggregated Lighthouse scores</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Page</TableHead>
                                <TableHead>Performance</TableHead>
                                <TableHead>Accessibility</TableHead>
                                <TableHead>Best Practices</TableHead>
                                <TableHead>SEO</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {testScores.map((result) => (
                                <TableRow key={result.page}>
                                    <TableCell>{result.page}</TableCell>
                                    <TableCell>{result.p}</TableCell>
                                    <TableCell>{result.a}</TableCell>
                                    <TableCell>{result.b}</TableCell>
                                    <TableCell>{result.s}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>

            <Card>
                <CardHeader>
                    <CardTitle>Contrast ratios between token backgrounds and foregrounds</CardTitle>
                </CardHeader>
                <CardContent>
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Token</TableHead>
                                <TableHead>Dark Mode Contrast Ratio</TableHead>
                                <TableHead>Light Mode Contrast Ratio</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {themeTokenContrastScores.map((result) => (
                                <TableRow key={result.token}>
                                    <TableCell>{result.token}</TableCell>
                                    <TableCell>{result.darkRatio}</TableCell>
                                    <TableCell>{result.lightRatio}</TableCell>
                                </TableRow>
                            ))}
                        </TableBody>
                    </Table>
                </CardContent>
            </Card>
        </div>
    );
}
