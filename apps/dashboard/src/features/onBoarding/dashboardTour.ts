import {Tour} from "nextstepjs";

/*
- the onboarding shows 'pop-ups' for the navBar, dashboard dropdown, time period selector, edit button and help menu
*/

export const dashboardTour : Tour[] = [
  { tour : "dashboard",
    steps : [

      { selector : "#navBar",
        title : "Navigation bar",
        content : "A navigation bar is provided for easy access you your cloud connections and managing you connections such as its configuration and resources",
        side : "right",
        showControls : true,
        showSkip : true,
      },

      { selector : "#dashboardDropdown",
        title : "Dashboard dropdown",
        content : "You are able to create multiple dashboards. Use this dropdown to switch between dashboards or create a new one",
        side : "bottom",
        showControls : true,
        showSkip : true,
      },

      { selector : "#editDashboard",
        title : "Edit dashboard",
        content : "Switch to edit mode to add widgets to your dashboard, remove any existing widgets or adjust the payout and sizing of your widgets",
        side : "bottom",
        showControls : true,
        showSkip : true,
      },

      { selector : "#timePeriodSelector",
        title : "Time period selector",
        content : "Adjust the time range to analyse your cloud metrics over different periods, such as a day or week",
        side : "bottom",
        showControls : true,
        showSkip : true,
      },

      { selector : "#helpButton",
        title : "Help & resources",
        content : "Need assistance? Access documentation and tutorials via the help center, FAQs and keyboard shortcuts at any time from the help menu",
        side : "left",
        showControls : true,
        showSkip : true,
      },

    ],
  },
];