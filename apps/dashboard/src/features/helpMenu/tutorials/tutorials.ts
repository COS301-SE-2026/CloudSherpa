//removed this from the docs&tuts file to prevent code dupliction
export interface Tutorials {
    id: string;
    name: string;
    description: string;
    category:
        | "Getting Started"
        | "Connections"
        | "Resources"
        | "Billing"
        | "Recommendations"
        | "Forecasting";
    lengthOfVideo: string;
    videoLink: string;
    thumbNail: string;
}

//structure is id, name, description, category, duration, youtubeid
const TUTS = [
    //logging in -
    {
        id: "gettingStarted",
        name: "Logging into CloudSherpa",
        description: "Learn by watching how to login with CloudSherpa",
        category: "Getting started",
        durationOfVideo: "0:13",
        youtubeId: "k-uOlb0-bIY",
    },

    //registering - https://youtu.be/WG14T84CNJ4
    {
        id: "gettingStarted2",
        name: "Registering with CloudSherpa",
        description: "Registering an account with CloudSherpa",
        category: "Getting started",
        durationOfVideo: "0:19",
        youtubeId: "WG14T84CNJ4",
    },

    //config line chart - https://youtu.be/INsecuXAUyg
    {
        id: "gettingStarted3",
        name: "Configure your line chart",
        description: "Change the metrics that you monitor, name or type of graph",
        category: "Getting started",
        durationOfVideo: "0:46",
        youtubeId: "INsecuXAUyg",
    },

    //config gauge chart - https://youtu.be/cPGMUgXEnx4
    {
        id: "gettingStarted4",
        name: "Configure your gauge chart",
        description: "Change the metrics that you monitor, name or type of graph",
        category: "Getting started",
        durationOfVideo: "0:22",
        youtubeId: "cPGMUgXEnx4",
    },

    //add dashboard and widgets - https://youtu.be/5cbma4cMZjo
    {
        id: "gettingStarted5",
        name: "Customize your dashboard",
        description: "Add multiple dashboard and widgets and configure them",
        category: "Getting started",
        durationOfVideo: "0:35",
        youtubeId: "5cbma4cMZjo",
    },

    //editing resources - https://youtu.be/Zh2nBeNTG8g
    {
        id: "resources",
        name: "Managing your resources",
        description: "You can configure your resources at any time",
        category: "Resources",
        durationOfVideo: "0:24",
        youtubeId: "Zh2nBeNTG8g",
    },

    //deleting connections - https://youtu.be/UbWKtwcsVuI
    {
        id: "connections",
        name: "Managing your connections",
        description: "Delete an unwanted connection via the connection manager",
        category: "Connections",
        durationOfVideo: "0:14",
        youtubeId: "UbWKtwcsVuI",
    },

    //connection details - https://youtu.be/imGteoTzvHw
    {
        id: "connections2",
        name: "Connection information",
        description: "View details about a specific connection",
        category: "Connections",
        durationOfVideo: "0:40",
        youtubeId: "imGteoTzvHw",
    },

    //add connection - https://youtu.be/zIrfipSx_1s
    {
        id: "connections3",
        name: "Adding an AWS connection",
        description: "Add multiple connection instances and manage them",
        category: "Connections",
        durationOfVideo: "0:53",
        youtubeId: "zIrfipSx_1s",
    },

    //gcp connection - https://youtu.be/CywPZOkA7ao
    {
        id: "connections4",
        name: "Adding a GCP connection",
        description: "Add a GCP connection, configure and manage its resources",
        category: "Connections",
        durationOfVideo: "1:11",
        youtubeId: "CywPZOkA7ao",
    },

    //azure connection - https://youtu.be/_JhdKb_6rwU
    {
        id: "connections5",
        name: "Adding an Azure connection",
        description: "Add an Azure connection, configure and manage its resources",
        category: "Connections",
        durationOfVideo: "0:57",
        youtubeId: "_JhdKb_6rwU",
    },

    //config kpi - https://youtu.be/NQRHgKiymqE
    {
        id: "billing",
        name: "Configuring your KPI widget",
        description: "From the dashboard edit button you can add a KPI widget and manage it",
        category: "Billing",
        durationOfVideo: "0:39",
        youtubeId: "NQRHgKiymqE",
    },

    //recommendations - https://youtu.be/ivMxbPg3OmY
    {
        id: "recommendations",
        name: "Recommendations",
        description: "Navigating through recommendations for optimization",
        category: "Recommendations",
        durationOfVideo: "1:12",
        youtubeId: "ivMxbPg3OmY",
    },

    //usage - https://youtu.be/UaqoLT9Wb7A
    {
        id: "forecasting",
        name: "Usage forecasting",
        description: "Navigating about your usage forecasting",
        category: "Forecasting",
        durationOfVideo: "1:11",
        youtubeId: "UaqoLT9Wb7A",
    },

    //billing - https://youtu.be/ivMxbPg3OmY
    {
        id: "forecasting2",
        name: "Billing forecasting",
        description: "Navigating about your billing forecasting",
        category: "Forecasting",
        durationOfVideo: "1:09",
        youtubeId: "ivMxbPg3OmY",
    },
];

export const TUTORIALS: Tutorials[] = TUTS.map((tutorial) => ({
    id: tutorial.id,
    name: tutorial.name,
    description: tutorial.description,
    category: tutorial.category as Tutorials["category"],
    lengthOfVideo: tutorial.durationOfVideo,
    videoLink: `https://www.youtube.com/embed/${tutorial.youtubeId}`,

    //maxresdefault is used by youtibe to serve the highest res version of a thumbnail (1280x720)
    thumbNail: `https://img.youtube.com/vi/${tutorial.youtubeId}/maxresdefault.jpg`,
}));

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
