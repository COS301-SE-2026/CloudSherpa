//removed this from the docs&tuts file to prevent code dupliction
export interface Tutorials {
    id: string;
    name: string;
    description: string;
    category: "Getting Started" | "Connections" | "Resources" | "Billing" | "Recommendations" | "Forecasting";
    lengthOfVideo: string;
    videoLink: string;
    thumbNail: string;
}

//structure is id, name, description, category, duration, youtubeid
const TUTS = [
    //logging in -
    [
        "gettingStarted",
        "Logging into CloudSherpa",
        "Learn by watching how to login with CloudSherpa",
        "Getting started",
        "0:13",
        "k-uOlb0-bIY",
    ],

    //registering - https://youtu.be/WG14T84CNJ4
    [
        "gettingStarted2",
        "Registering with CloudSherpa",
        "Registering an account with CloudSherpa",
        "Getting started",
        "0:19",
        "WG14T84CNJ4",
    ],

    //config line chart - https://youtu.be/INsecuXAUyg
    [
        "gettingStarted3",
        "Configure your line chart",
        "Change the metrics that you monitor, name or type of graph",
        "Getting started",
        "0:46",
        "INsecuXAUyg",
    ],

    //config gauge chart - https://youtu.be/cPGMUgXEnx4
    [
        "gettingStarted4",
        "Configure your gauge chart",
        "Change the metrics that you monitor, name or type of graph",
        "Getting started",
        "0:22",
        "cPGMUgXEnx4",
    ],

    //add dashboard and widgets - https://youtu.be/5cbma4cMZjo
    [
        "gettingStarted5",
        "Customize your dashboard",
        "Add multiple dashboard and widgets and configure them",
        "Getting started",
        "0:35",
        "5cbma4cMZjo",
    ],

    //editing resources - https://youtu.be/Zh2nBeNTG8g
    [
        "resources",
        "Managing your resources",
        "You can configure your resources at any time",
        "Resources",
        "0:24",
        "Zh2nBeNTG8g",
    ],

    //deleting connections - https://youtu.be/UbWKtwcsVuI
    [
        "connections",
        "Managing your connections",
        "Delete an unwanted connection via the connection manager",
        "Connections",
        "0:14",
        "UbWKtwcsVuI",
    ],

    //connection details - https://youtu.be/imGteoTzvHw
    [
        "connections2",
        "Connection information",
        "View details about a specific connection",
        "Connections",
        "0:40",
        "imGteoTzvHw",
    ],

    //add connection - https://youtu.be/zIrfipSx_1s
    [
        "connections3",
        "Adding an AWS connection",
        "Add multiple connection instances and manage them",
        "Connections",
        "0:53",
        "zIrfipSx_1s",
    ],

    //gcp connection - https://youtu.be/CywPZOkA7ao
    [
        "connections4",
        "Adding a GCP connection",
        "Add a GCP connection, configure and manage its resources",
        "Connections",
        "1:11",
        "CywPZOkA7ao",
    ],

    //azure connection - https://youtu.be/_JhdKb_6rwU
    [
        "connections5",
        "Adding an Azure connection",
        "Add an Azure connection, configure and manage its resources",
        "Connections",
        "0:57",
        "_JhdKb_6rwU",
    ],

    //config kpi - https://youtu.be/NQRHgKiymqE
    [
        "billing",
        "Configuring your KPI widget",
        "From the dashboard edit button you can add a KPI widget and manage it",
        "Billing",
        "0:39",
        "NQRHgKiymqE",
    ],

    //recommendations - https://youtu.be/ivMxbPg3OmY
    [
        "recommendations",
        "Recommendations",
        "Navigating through recommendations for optimization",
        "Recommendations",
        "1:12",
        "ivMxbPg3OmY",
    ],

    //usage - https://youtu.be/UaqoLT9Wb7A
    [
        "forecasting",
        "Usage forecasting",
        "Navigating about your usage forecasting",
        "Forecasting",
        "1:11",
        "UaqoLT9Wb7A",
    ],

    //billing - https://youtu.be/ivMxbPg3OmY
    [
        "forecasting2",
        "Billing forecasting",
        "Navigating about your billing forecasting",
        "Forecasting",
        "1:09",
        "ivMxbPg3OmY",
    ],
];

export const TUTORIALS: Tutorials[] = TUTS.map(
    ([id, name, description, category, durationOfVideo, youtubeid]) => ({
        id,
        name,
        description,
        category: category as Tutorials["category"],
        lengthOfVideo: durationOfVideo,
        videoLink: `https://www.youtube.com/embed/${youtubeid}`,

        //maxresdefault is used by youtibe to serve the highest res version of a thumbnail (1280x720)
        thumbNail: `https://img.youtube.com/vi/${youtubeid}/maxresdefault.jpg`,
    })
);

export const TUTFILTERS = [
    "All",
    ...new Set(TUTORIALS.map((forTutorials) => forTutorials.category)),
];

export const filterTutorialsByCategory = (category: string) => {
    if (category === "All") {
        return TUTORIALS;
    }

    return TUTORIALS.filter((forTutorials) => forTutorials.category === category);
};
