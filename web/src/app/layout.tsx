import type { Metadata } from "next";
import { Geist, Playfair_Display } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const playfair = Playfair_Display({
  variable: "--font-playfair",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700"],
  style: ["normal", "italic"],
});

export const metadata: Metadata = {
  title: "Memora — AI Family Memory Photos",
  description:
    "Bring loved ones into the moments they couldn't reach. Private, natural, AI-assisted family memory photos.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${geistSans.variable} ${playfair.variable} h-full antialiased`}>
      <head>
        <script src="https://accounts.google.com/gsi/client?hl=en" async defer></script>
      </head>
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
