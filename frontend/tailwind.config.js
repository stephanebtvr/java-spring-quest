/** @type {import('tailwindcss').Config} */
module.exports = {
  // Spécifie les fichiers à scanner pour les classes Tailwind
  content: [
    "./src/**/*.{html,ts}", // Tous les fichiers HTML et TypeScript
  ],

  // Thème personnalisé pour JavaQuest
  theme: {
    extend: {
      // Palette de couleurs personnalisée
      colors: {
        // Couleurs principales de la marque
        primary: {
          50: "#f0f9ff",
          100: "#e0f2fe",
          200: "#bae6fd",
          300: "#7dd3fc",
          400: "#38bdf8",
          500: "#0ea5e9", // Couleur principale
          600: "#0284c7",
          700: "#0369a1",
          800: "#075985",
          900: "#0c4a6e",
        },
        // Couleurs secondaires (succès, erreur, etc.)
        success: {
          light: "#4ade80",
          DEFAULT: "#22c55e",
          dark: "#16a34a",
        },
        danger: {
          light: "#f87171",
          DEFAULT: "#ef4444",
          dark: "#dc2626",
        },
        warning: {
          light: "#fbbf24",
          DEFAULT: "#f59e0b",
          dark: "#d97706",
        },
        // Niveaux de difficulté
        beginner: "#10b981",
        intermediate: "#3b82f6",
        advanced: "#f59e0b",
        architect: "#ef4444",
      },

      // Fonts personnalisées
      fontFamily: {
        sans: ["Inter", "system-ui", "sans-serif"],
        mono: ["JetBrains Mono", "monospace"],
      },

      // Animations personnalisées
      animation: {
        "fade-in": "fadeIn 0.3s ease-in",
        "slide-up": "slideUp 0.3s ease-out",
        "pulse-slow": "pulse 3s cubic-bezier(0.4, 0, 0.6, 1) infinite",
      },

      keyframes: {
        fadeIn: {
          "0%": { opacity: "0" },
          "100%": { opacity: "1" },
        },
        slideUp: {
          "0%": { transform: "translateY(10px)", opacity: "0" },
          "100%": { transform: "translateY(0)", opacity: "1" },
        },
      },

      // Shadows personnalisées
      boxShadow: {
        card: "0 2px 8px rgba(0, 0, 0, 0.1)",
        "card-hover": "0 4px 16px rgba(0, 0, 0, 0.15)",
      },
    },
  },

  // Plugins Tailwind
  plugins: [
    // Plugin pour les formulaires (optionnel)
    // require('@tailwindcss/forms'),
    // Plugin pour la typographie (optionnel)
    // require('@tailwindcss/typography'),
  ],
};
