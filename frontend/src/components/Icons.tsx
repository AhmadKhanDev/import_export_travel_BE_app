import { type SVGProps } from "react";

type IconProps = SVGProps<SVGSVGElement> & { size?: number };

const icon = (path: string) =>
  function Icon({ size = 20, className = "", ...rest }: IconProps) {
    return (
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth={1.75}
        strokeLinecap="round"
        strokeLinejoin="round"
        className={className}
        aria-hidden
        {...rest}
      >
        <path d={path} />
      </svg>
    );
  };

const icon2 = (paths: string[]) =>
  function Icon({ size = 20, className = "", ...rest }: IconProps) {
    return (
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width={size}
        height={size}
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth={1.75}
        strokeLinecap="round"
        strokeLinejoin="round"
        className={className}
        aria-hidden
        {...rest}
      >
        {paths.map((d, i) => (
          <path key={i} d={d} />
        ))}
      </svg>
    );
  };

export const PlaneIcon = icon2([
  "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z",
]);

export const ShoppingBagIcon = icon2([
  "M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4z",
  "M3 6h18",
  "M16 10a4 4 0 0 1-8 0",
]);

export const ShieldIcon = icon(
  "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z",
);

export const StarIcon = icon(
  "M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z",
);

export const MapPinIcon = icon2([
  "M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z",
  "M12 7a3 3 0 1 0 0 6 3 3 0 0 0 0-6z",
]);

export const PackageIcon = icon2([
  "M16.5 9.4 7.55 4.24",
  "M21 16V8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16z",
  "M3.27 6.96 12 12.01l8.73-5.05",
  "M12 22.08V12",
]);

export const TruckIcon = icon2([
  "M1 3h15v13H1z",
  "M16 8h4l3 3v5h-7V8z",
  "M5.5 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z",
  "M18.5 21a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5z",
]);

export const CreditCardIcon = icon2([
  "M21 4H3a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h18a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2z",
  "M1 10h22",
]);

export const CheckCircleIcon = icon2([
  "M22 11.08V12a10 10 0 1 1-5.93-9.14",
  "M22 4 12 14.01l-3-3",
]);

export const ClockIcon = icon2(["M12 2a10 10 0 1 0 0 20A10 10 0 0 0 12 2z", "M12 6v6l4 2"]);

export const UserIcon = icon2([
  "M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2",
  "M12 3a4 4 0 1 0 0 8 4 4 0 0 0 0-8z",
]);

export const MenuIcon = icon("M3 12h18M3 6h18M3 18h18");

export const XIcon = icon("M18 6 6 18M6 6l12 12");

export const ChevronRightIcon = icon("m9 18 6-6-6-6");

export const ArrowRightIcon = icon("M5 12h14m-7-7 7 7-7 7");

export const BellIcon = icon2([
  "M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9",
  "M13.73 21a2 2 0 0 1-3.46 0",
]);

export const SearchIcon = icon2([
  "M21 21l-4.35-4.35",
  "M17 11A6 6 0 1 1 5 11a6 6 0 0 1 12 0z",
]);

export const LockIcon = icon2([
  "M19 11H5a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7a2 2 0 0 0-2-2z",
  "M7 11V7a5 5 0 0 1 10 0v4",
]);

export const KeyIcon = icon2([
  "M21 2l-2 2m-7.61 7.61a5.5 5.5 0 1 1-7.778 7.778 5.5 5.5 0 0 1 7.777-7.777zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3m-3.5 3.5L19 4",
]);

export const GlobeIcon = icon2([
  "M12 2a10 10 0 1 0 0 20A10 10 0 0 0 12 2z",
  "M2 12h20",
  "M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z",
]);

export const BookOpenIcon = icon2([
  "M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z",
  "M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z",
]);

export const RefreshIcon = icon2([
  "M23 4v6h-6",
  "M1 20v-6h6",
  "M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15",
]);

export const LogOutIcon = icon2([
  "M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4",
  "M16 17l5-5-5-5",
  "M21 12H9",
]);

export const TagIcon = icon2([
  "M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z",
  "M7 7h.01",
]);

export const CalendarIcon = icon2([
  "M8 2v4M16 2v4",
  "M3 8h18",
  "M21 6H3a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2h18a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2z",
]);

export const DollarIcon = icon2(["M12 1v22", "M17 5H9.5a3.5 3.5 0 1 0 0 7h5a3.5 3.5 0 1 1 0 7H6"]);

export const AlertCircleIcon = icon2(["M12 2a10 10 0 1 0 0 20A10 10 0 0 0 12 2z", "M12 8v4", "M12 16h.01"]);

export const InfoIcon = icon2(["M12 2a10 10 0 1 0 0 20A10 10 0 0 0 12 2z", "M12 16v-4", "M12 8h.01"]);
