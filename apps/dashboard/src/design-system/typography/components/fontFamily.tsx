import { Font_Family } from "@/design-system/typography/types/typography";
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from "@/components/atoms/card";
interface FontFamilyProps {
    FontFamilies: Font_Family[];
}

export default function FontFamily({ FontFamilies }: Readonly<FontFamilyProps>) {
    return (
        <div className="space-y-6">
            {FontFamilies.map((family) => (
                <div
                    key={family.name}
                    className="grid grid-cols-1 md:grid-cols-2 style={{ fontFamily: family.value }} "
                >
                    <h1 className="text-[120px] md:text-[160px] leading-none text-card-foreground font-bold tracking-tighter flex items-center justify-center">
                        Aa
                    </h1>
                    <Card className="w-full">
                        <CardHeader>
                            <CardTitle className="font-bold capitalize text-foreground">
                                Geist {family.name}
                            </CardTitle>
                            <CardDescription className="text-muted-foreground">
                                font-{family.name}
                            </CardDescription>
                        </CardHeader>

                        <CardContent
                            className="text-base md:text-base leading-relaxed text-card-foreground wrap-break-words mt-4"
                            style={{ fontFamily: family.value }}
                        >
                            A B C D E F G H I J K L M N O P Q R S T U V W X Y Z <br />
                            a b c d e f g h i j k l m n o p q r s t u v w x y z <br />0 1 2 3 4 5 6
                            7 8 9 ! @ # $ % & * ( ) _ +
                        </CardContent>
                    </Card>
                </div>
            ))}
        </div>
    );
}
