import { BrandVariants, createDarkTheme, createLightTheme } from "@fluentui/react-components"

const customRamp : BrandVariants = {
    "10": "#000000",
    "20": "#0F141B",
    "30": "#17212F",
    "40": "#1D2F45",
    "50": "#233D5C",
    "60": "#294C74",
    "70": "#2E5B8D",
    "80": "#336BA7",
    "90": "#377BC2",
    "100": "#3A8CDD",
    "110": "#4B9DF2",
    "120": "#67ADFF",
    "130": "#8CBDFF",
    "140": "#ACCDFF",
    "150": "#C9DEFF",
    "160": "#E5EEFF"
  }

export const customLightTheme = createLightTheme(customRamp)
export const customDarkTheme = createDarkTheme(customRamp)
