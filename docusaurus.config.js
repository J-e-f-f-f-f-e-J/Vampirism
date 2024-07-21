// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

import {themes as prismThemes} from 'prism-react-renderer';

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Vampirism',
  tagline: 'Wiki',
  url: 'https://wiki.vampirism.dev',
  baseUrl: '/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',
  organizationName: 'TeamLapen',
  projectName: 'Vampirism',
  deploymentBranch: 'gh-pages',
  trailingSlash: false,

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
          editUrl: 'https://github.com/TeamLapen/Vampirism/tree/gh-pages/',
          lastVersion: 'current',
          versions: {
            current: {
              /** this value must be changed if a new version is released */
              label: '1.10 NeoForge',
            }
          },
        },
        blog: {
          showReadingTime: true,
          editUrl: 'https://github.com/TeamLapen/Vampirism/tree/gh-pages/',
          blogTitle: 'Vampirism Blog',
          blogSidebarCount: 'ALL',
          blogSidebarTitle: 'All posts',
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  plugins: [
    [
      '@docusaurus/plugin-content-docs',
      {
        id: 'integrations',
        path: 'integrations',
        routeBasePath: 'integrations',
        sidebarPath: './sidebarsIntegrations.js',
        versions: {
          current: {
            label: '1.8',
          }
        }
      },
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        title: 'Vampirism',
        logo: {
          alt: 'Vampirism Logo',
          src: 'img/fang.png',
        },
        items: [
          {
            href: 'https://vampirism.dev',
            label: 'Website',
            position: 'left',
          },
          {
            type: 'doc',
            docId: 'wiki/intro',
            position: 'left',
            label: 'Wiki',
          },
          {
            type: 'doc',
            docId: 'api/intro',
            position: 'left',
            label: 'API',
          },
          {
            type: 'doc',
            docId: 'data/intro',
            position: 'left',
            label: 'Data Packs',
          },
          {
            type: 'doc',
            docId: 'wiki/intro',
            docsPluginId: 'integrations',
            position: 'left',
            label: 'Integrations',
          },
          {to: '/blog', label: 'Blog', position: 'left'},
          {
            type: 'docsVersionDropdown',
            position: 'right',
            dropdownItemsAfter: [
              { to: 'https://github.com/TeamLapen/Vampirism/wiki', label: 'For MC 1.7.10' },
              { to: 'https://github.com/TeamLapen/Vampirism/wiki', label: 'For MC 1.12' },
            ],
            dropdownActiveClassDisabled: true,
          },
          {
            type: 'docsVersionDropdown',
            position: 'right',
            dropdownItemsAfter: [
              { to: 'https://github.com/TeamLapen/VampirismIntegrations/wiki', label: 'For MC 1.19 or older' },
            ],
            dropdownActiveClassDisabled: false,
            docsPluginId: 'integrations',
          },
          {
            href: 'https://github.com/Teamlapen/Vampirism',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Docs',
            items: [
              {
                label: 'Wiki',
                to: '/docs/wiki/intro',
              },
              {
                label: 'API',
                to: '/docs/api/intro',
              },
              {
                label: 'Data Pack',
                to: '/docs/data/intro',
              },
            ],
          },
          {
            title: 'Community',
            items: [
              {
                label: 'Discord',
                href: 'https://discord.gg/wuamm4P',
              },
              {
                label: 'Twitter',
                href: 'https://twitter.com/Maxanier',
              },
            ],
          },
          {
            title: 'More',
            items: [
              {
                label: 'Blog',
                to: '/blog',
              },
              {
                label: 'GitHub',
                href: 'https://github.com/Teamlapen/Vampirism',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} Vampirism, Contributors`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
      },
      metadata: [
        { name: 'keywords', content: 'minecraft, vampirism, forge, wiki' },
        { name: 'twitter:card', content: 'summary' }
      ],
      image: 'img/fang.png',
    }),
};

export default config;