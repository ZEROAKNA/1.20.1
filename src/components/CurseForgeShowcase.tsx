import React, { useState } from 'react';
import { Copy, Check, Download, ShieldCheck, Sparkles, Globe, Layers } from 'lucide-react';
import modLogoUrl from '../assets/images/curseforge_mod_logo_1791226916863.jpg';
import modBannerUrl from '../assets/images/curseforge_mod_banner_1791226929488.jpg';

export const CURSEFORGE_SHORT_SUMMARY_EN =
  "Awaken all 7 Deadly Sins within yourself! Features 20s Time Stop with Anti-Regen, Gaze Instant Death, Full-Stack NBT Item Replication, Boss Skill Stealing, Blood Contract Summons, and native integration with Iron's Spells, Apotheosis, Cataclysm & Ice and Fire.";

export const CURSEFORGE_SHORT_SUMMARY_RU =
  "Пробудите все 7 Смертных Грехов внутри себя! Остановка времени на 20 сек без регенерации врагов, Мгновенная Смерть по взгляду, копирование полных стаков с NBT/Apotheosis чарами, кража способностей боссов и 100% работа как в чистом Forge 1.20.1, так и в Cisco's RPG.";

export const CURSEFORGE_MARKDOWN_EN = `# ✦ Seven Deadly Sins: Original Sin Awakening ✦
**Minecraft 1.20.1 (Forge 47.3.0+) | Standalone & RPG Modpack Ready**

> *"Break the Seal of the Ancient City. Devour the Forbidden Fruit. Command all Seven Deadly Sins at once."*

**Seven Deadly Sins: Original Sin Awakening** is a high-performance, dark-fantasy RPG ability mod for **Minecraft 1.20.1 (Forge)**. Unlike traditional anime mods that only add weapons or armor sets, this mod awakens **all 7 Deadly Sins directly inside your character**, complete with a custom **Radial Menu (\`[R]\`)**, **Sub-Mode Switching (\`[X]\`)**, **Active Casting (\`[V]\`)**, and the ultimate **Demon Mark: Sin Overdrive (\`[Shift + V]\`)**.

---

## 🛡️ 100% Standalone + Native Modpack Compatibility
* **Works 100% Standalone (Vanilla + Forge 1.20.1):** Does **NOT** require any external mods! Every single Sin, HUD overlay, Soul Orb entity, and Ancient City loot modifier works out of the box in pure Forge 1.20.1.
* **Built-in Soft Integrations (Auto-Detected at Runtime):** If installed alongside **Cisco's Fantasy Medieval RPG [Dragonfyre]** or your own RPG modpack, the mod automatically bridges with:
  * **Apotheosis (AttributesLib):** Dynamically scales *Crit Chance*, *Crit Damage*, *Armor Shred*, and *Life Steal*, and duplicates Mythic items with all sockets & gems intact.
  * **Iron's Spells 'n Spellbooks:** Scales *Spell Power* & *Max Mana*, and instantly resets all spell cooldowns upon consuming Boss Souls.
  * **Ice and Fire: Dragons & L_Ender's Cataclysm:** Steal Fire/Ice/Lightning Dragon Breath, Gorgon's Gaze, and Cataclysm boss ultimates (*Ignis, Maledictus, Harbinger, Leviathan*).
  * **L2Hostility:** Multi-phase cascade damage bypasses Boss Damage Caps and suppresses *Reflect* & *Undying* traits.

---

## 🔥 The Seven Deadly Sins & Their Powers

### ☀️ I. PRIDE (Гордыня) — *Ruler of Time & Judgment*
Switch between 3 absolute laws using \`[X]\`:
1. **20-Second Time Stop (Law 1):** Freezes all mobs and mid-air projectiles within **64 blocks** for **20 full seconds**. Enemy movement is locked to zero and **enemy HP regeneration is completely disabled** while time is stopped. Press \`[V]\` again to resume time early.
2. **Absolute Purge (Law 2):** Instantly strips **all potion effects, buffs, absorption shields, and L2Hostility traits** from the entity you are looking at (up to 48 blocks) and locks their regeneration.
3. **Gaze of Instant Death (Law 3):** Executes the target you are looking at on the spot — destroying Totems of Undying in their hands and bypassing Boss Damage Caps.

### 💰 II. GREED (Алчность) — *Full-Stack NBT Replicator & Vault*
1. **Full-Stack Hand & Block Replication (Mode 1):** Holding a stack of items (e.g., **13 items**) and pressing \`[V]\` immediately grants you **+13 exact copies** into your inventory (preserving 100% of *Apotheosis* affixes, sockets, and NBT tags) and saves the stack to your Vault! Looking at any block copies it directly into your inventory.
2. **Greed Vault & Slot Manager (Mode 2):** Open the **Greed Catalog GUI** to materialize full saved stacks at any time, or delete unwanted items using the **\`[✕]\`** button or **Clear All** button.

### ❤️ III. LUST (Похоть) — *Blood Contract & Boss Nightmare*
* **Blood Contract (Mobs & Elites):** Permanently tames the target and up to 5 nearby mobs as loyal bodyguards with **+300% Max Health** and **+150% Attack Damage**. They regenerate health, teleport to follow you, and never despawn.
* **Nightmare Illusion (Bosses):** Casting Lust on a Boss disorients them for 12 seconds, forcing them to attack nearby monsters while taking **+45% increased damage** from all your attacks.

### 🐍 IV. ENVY (Зависть) — *Cross-Mod Ability Thief*
1. **Steal Ability (Mode 1):** Drains 20% HP, suppresses *L2Hostility* traits, and steals abilities from **Endermen** (*Blink*), **Wardens** (*Sonic Boom*), **Creepers** (*Blast Immunity*), **Ice & Fire Dragons**, **Cataclysm Bosses**, and **Iron's Spells** casters.
2. **Cast Stolen Skill (Mode 2):** Unleash your currently equipped stolen ability using multi-phase cascade damage.
3. **Envy Arsenal GUI (Mode 3):** Open a dedicated selection screen to switch between up to 24 stolen abilities at any time.

### 🌀 V. GLUTTONY (Чревоугодие) — *Devourer of Souls*
* **Passive — Soul Orbs:** Killing mobs spawns glowing **Soul Orbs** (+3 Max Mana). Slaying a Boss spawns a **Crimson Boss Soul Orb** that permanently grants **+50 Max Mana**, **+1 Dragonfyre Soul Rank**, and resets all *Iron's Spells* cooldowns.
* **Active — Abyssal Vortex:** Pulls all enemies within 14 blocks into a singularity, dealing % Max HP cascade damage and restoring your Health, Hunger, and Mana.

### 💢 VI. WRATH (Гнев) — *Berserker Cataclysm & Demon Mark*
* **Passive — Rage Scaling (0–100%):** Taking or dealing damage builds Wrath stacks, dynamically boosting Attack Damage, Movement Speed, *Spell Power*, *Crit Chance*, and *Armor Shred*. Reaching 100% triggers **Berserker Mode (x3 Damage & Explosive Cleave)**.
* **Active — Wrath Shockwave:** Instantly adds +35% Wrath stacks and unleashes a 5-phase armor-piercing eruption.

### 💤 VII. SLOTH (Лень) — *King's Hibernation & 5.0x Awakening*
* **Passive — Sloth's Rest:** Standing still for 5 seconds grants **+22 Mana/sec** and **+5 HP/sec**.
* **Active — King's Hibernation:** Instantly restores **100% HP & Mana**, purges all negative curses, grants **20 Golden Absorption Hearts**, and after a 10-second seal unleashes **5.0x Damage & 5.0x Mana Regen** for 3 minutes!

---

## 😈 Ultimate Awakening: Demon Mark / Sin Overdrive (\`[Shift + V]\`)
Pressing **\`Shift + V\`** (when at 80%+ Wrath, Soul Rank 2+, or for 50 Mana) activates **Original Sin Overdrive** for **30 seconds**:
* **1.5x Multiplier** to all Sin damage and attribute bonuses;
* **-50% Mana Cost** on all abilities;
* **Hellblaze Black Flame:** Every hit ignites enemies with black soul-fire that **completely blocks Boss HP regeneration**.

---

## 🎮 Controls & How to Start
1. **Find the Forbidden Fruit:** Loot it from **Ancient City** chests (15%), **Bastions**, **End Cities**, or Dragon/Cataclysm boss chests (or grab it from the *Food & Drinks* Creative Tab).
2. **Press \`[R]\`** — Open the **Seven Deadly Sins Radial Wheel** (or press \`1–7\`).
3. **Press \`[X]\`** — Cycle between the active Sin's sub-modes.
4. **Press \`[V]\`** — Cast the active Sin ability (or open the Greed Vault / Envy Arsenal GUI).
5. **Press \`[Shift + V]\`** — Activate **Demon Mark: Sin Overdrive**.`;

export const CURSEFORGE_MARKDOWN_RU = `# ✦ Seven Deadly Sins: Пробуждение Первородного Греха ✦
**Minecraft 1.20.1 (Forge 47.3.0+) | Полностью самостоятельный мод + Оптимизация под RPG-сборки**

> *«Сорвите печать Древнего Города. Вкусите Запретный Плод. Управляйте всеми Семью Смертными Грехами одновременно.»*

**Seven Deadly Sins: Original Sin Awakening** — это масштабный темный фэнтези RPG-мод для **Minecraft 1.20.1 (Forge)**. В отличие от обычных модов по аниме, которые добавляют только мечи или броню, этот мод пробуждает **все 7 Смертных Грехов прямо внутри вашего персонажа** с собственным **Радиальным Меню (\`[R]\`)**, переключением подрежимов (\`[X]\`), кастом способностей (\`[V]\`) и ультимативным режимом **«Метка Демона: Первородный Грех» (\`[Shift + V]\`)**.

---

## 🛡️ Работает на 100% БЕЗ других модов + Поддержка Cisco's RPG
* **Полностью самостоятельный мод (Standalone):** Моду **НЕ требуются** никакие другие моды! Вы можете установить его на чистый **Forge 1.20.1** или в любую свою сборку — все 7 Грехов, остановка времени, копирование стаков, приручение и сфера душ работают автономно.
* **Умная авто-интеграция (Soft Dependencies):** Если мод обнаруживает рядом моды из сборки **Cisco's Fantasy Medieval RPG [Dragonfyre]**, он автоматически включает синергию с *Apotheosis (AttributesLib)*, *Iron's Spells 'n Spellbooks*, *Ice and Fire: Dragons*, *L_Ender's Cataclysm*, *Simply Swords* и *L2Hostility*.

---

## 🔥 Все 7 Смертных Грехов и их способности

### ☀️ I. ГОРДЫНЯ (Pride) — *Владыка Времени и Приговора*
Переключайте 3 способности клавишей \`[X]\`:
1. **Тайм Стоп на 20 секунд (Способность 1):** Полностью останавливает всех существ и летящие снаряды в радиусе **64 блоков** на **20 секунд**. Никто кроме игрока не может двигаться, а **регенерация HP врагов полностью отключена**! Повторное нажатие \`[V]\` снимает остановку времени досрочно.
2. **Абсолютное Очищение по взгляду (Способность 2):** Мгновенно стирает **все эффекты, баффы, щиты поглощения и трейты L2Hostility** с того, на кого смотрит игрок (до 48 блоков).
3. **Мгновенная Смерть по взгляду (Способность 3):** Убивает взглядом цель, на которую смотрит игрок — уничтожая Тотемы Бессмертия в её руках и пробивая лимит урона (Damage Cap) боссов.

### 💰 II. АЛЧНОСТЬ (Greed) — *Копирование Полных Стаков и Сокровищница*
1. **Копирование стака в руке и блоков (Режим 1):** Если вы держите предмет в руке и в стаке, например, **13 штук** — при нажатии \`[V]\` вы сразу получаете **+13 штук** этих предметов (со всеми чарами и мифическими аффиксами *Apotheosis*) и сохраняете стак в Каталог! Наведение на блок копирует его прямо в инвентарь.
2. **Каталог Алчности с удалением предметов (Режим 2):** Открывает меню Сокровищницы, где можно создавать сохранённые стаки (например, сразу по \`x13\` шт.) или удалять ненужные слоты кнопкой **\`[✕]\`** / **«Очистить весь каталог»**.

### ❤️ III. ПОХОТЬ (Lust) — *Кровавый Контракт и Иллюзия Кошмара*
* **Кровавый Контракт (на мобов):** Превращает цель и до 5 мобов рядом в верных телохранителей с **+300% к Здоровью** и **+150% к Урону**. Они телепортируются за хозяином, регенерируют и рвут ваших врагов.
* **Иллюзия Кошмара (на Боссов):** Дезориентирует босса на 12 секунд, заставляя его атаковать окружающих мобов и увеличивая входящий по боссу урон на **+45%**.

### 🐍 IV. ЗАВИСТЬ (Envy) — *Похититель Способностей Боссов и Драконов*
* Крадёт *Телепортацию Эндермена*, *Sonic Boom Вардена*, *Взрывной иммунитет Крипера*, а также *Дыхание Драконов Ice & Fire*, *Ультимейты Боссов Cataclysm* и *Магию Iron's Spells*, подавляя трейты \`Reflect\` (Отражение урона) и \`Undying\` у мобов *L2Hostility*.

### 🌀 V. ЧРЕВОУГОДИЕ (Gluttony) — *Пожиратель Душ*
* Души убитых врагов навсегда повышают ваш пул маны (+3 за моба; **+50 макс. маны и +1 Ранг Души за босса** + сброс КД всех заклинаний). Активная воронка стягивает врагов и выкачивает их жизнь.

### 💢 VI. ГНЕВ (Wrath) — *Катаклизм Берсерка и Метка Демона*
* Накапливает ярость до 100%, разгоняя урон в **3 раза**, криты и пробивание брони. Активный взрыв бьёт 5-фазным каскадом в обход лимита урона боссов.

### 💤 VII. ЛЕНЬ (Sloth) — *Гибернация Короля и 5.0x Пробуждение*
* Мгновенно исцеляет 100% HP и маны, снимает все проклятия, даёт 20 золотых сердец и через 10 секунд активирует **5-кратный урон (5.0x)** на 3 минуты!`;

export const CurseForgeShowcase: React.FC = () => {
  const [copiedKey, setCopiedKey] = useState<string | null>(null);
  const [descLang, setDescLang] = useState<'en' | 'ru'>('en');

  const copyText = (key: string, text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedKey(key);
    setTimeout(() => {
      setCopiedKey((prev) => (prev === key ? null : prev));
    }, 2000);
  };

  const downloadImage = (url: string, filename: string) => {
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
  };

  return (
    <div className="space-y-8">
      {/* Standalone Answer Banner */}
      <div className="p-6 rounded-2xl bg-gradient-to-br from-emerald-950/50 via-[#0F172A] to-[#0B0F17] border border-emerald-500/40 space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <div className="p-2.5 rounded-xl bg-emerald-500/20 border border-emerald-500/40 text-emerald-300">
              <ShieldCheck className="w-6 h-6" />
            </div>
            <div>
              <span className="text-xs font-mono uppercase tracking-wider text-emerald-400">
                Проверка совместимости для публикации на CurseForge / Modrinth
              </span>
              <h2 className="text-xl font-bold text-slate-100">
                Можно ли играть в этот мод БЕЗ модпака Cisco&apos;s RPG? — ДА, на 100%!
              </h2>
            </div>
          </div>
          <span className="px-3 py-1 rounded-md bg-emerald-500/20 border border-emerald-500/40 text-xs font-mono text-emerald-300">
            mandatory = false (Все внешние моды опциональны)
          </span>
        </div>

        <p className="text-sm text-slate-300 leading-relaxed">
          Мод спроектирован по стандарту <strong>Universal Standalone + Soft Dependencies</strong>:
          в файле <code className="text-amber-300 font-mono">META-INF/mods.toml</code> обязательными указаны только{' '}
          <code className="text-emerald-300 font-mono">Forge 47+</code> и{' '}
          <code className="text-emerald-300 font-mono">Minecraft 1.20.1</code>. Все проверки модов из{' '}
          <em>Cisco&apos;s RPG</em> изолированы внутри <code className="text-amber-300 font-mono">ModList.get().isLoaded(...)</code> в{' '}
          <code className="text-amber-300 font-mono">CompatManager.java</code>.
        </p>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
          <div className="p-4 rounded-xl bg-slate-900/90 border border-slate-800 space-y-1.5">
            <div className="font-semibold text-amber-400">1. На чистом Minecraft 1.20.1 (без других модов):</div>
            <p className="text-slate-400 leading-relaxed">
              Работают абсолютно все 7 Грехов: 20-секундный Тайм Стоп с анти-регеном, Стирание эффектов и Мгновенная Смерть по взгляду, копирование полных стаков в руке (13 &rarr; +13 шт.) и удаление из Каталога Алчности, Кровавый Контракт Похоти, кража навыков Эндермена/Вардена/Крипера, Сферы Душ и Метка Демона (<code className="text-slate-200">Shift + V</code>). Запретный Плод генерируется в ванильном Древнем Городе, Бастионах и Городах Энда.
            </p>
          </div>
          <div className="p-4 rounded-xl bg-slate-900/90 border border-slate-800 space-y-1.5">
            <div className="font-semibold text-emerald-400">2. В сборке Cisco&apos;s RPG или любом другом RPG-модпаке:</div>
            <p className="text-slate-400 leading-relaxed">
              Мод автоматически обнаруживает установленные <em>Apotheosis</em>, <em>Iron&apos;s Spells</em>, <em>Ice &amp; Fire</em>, <em>Cataclysm</em> и <em>L2Hostility</em> и на лету включает бонусы к критам, пробою брони, силе заклинаний и кражу способностей драконов и боссов. Благодаря этому ваш мод идеально подходит для любой аудитории на CurseForge!
            </p>
          </div>
        </div>
      </div>

      {/* Visual Assets Section (Avatar 1:1 + Banner 16:9) */}
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        {/* 1:1 CurseForge Avatar */}
        <div className="lg:col-span-5 p-5 rounded-2xl bg-[#0F172A] border border-slate-800 flex flex-col justify-between space-y-4">
          <div className="space-y-1">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono uppercase tracking-wider text-amber-400">
                CurseForge Project Avatar (1:1 Square)
              </span>
              <span className="text-xs text-slate-400 font-mono">400×400+ Ready</span>
            </div>
            <h3 className="text-base font-bold text-slate-100">
              Аватарка / Иконка профиля мода для CurseForge
            </h3>
            <p className="text-xs text-slate-400">
              Запретный Плод Первородного Греха в кольце 7 рун Смертных Грехов.
            </p>
          </div>

          <div className="relative mx-auto w-64 h-64 rounded-2xl overflow-hidden border-2 border-amber-500/50 shadow-2xl bg-slate-950">
            <img
              src={modLogoUrl}
              alt="Seven Deadly Sins Mod CurseForge Avatar Icon"
              referrerPolicy="no-referrer"
              className="w-full h-full object-cover"
            />
          </div>

          <button
            onClick={() => downloadImage(modLogoUrl, 'sevendeadlysins-curseforge-avatar.jpg')}
            className="w-full py-2.5 px-4 rounded-xl bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold text-xs transition-colors flex items-center justify-center gap-2"
          >
            <Download className="w-4 h-4" />
            Скачать Аватарку Мода для CurseForge (.jpg)
          </button>
        </div>

        {/* 16:9 Promotional Banner */}
        <div className="lg:col-span-7 p-5 rounded-2xl bg-[#0F172A] border border-slate-800 flex flex-col justify-between space-y-4">
          <div className="space-y-1">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono uppercase tracking-wider text-amber-400">
                CurseForge Description Header Banner (16:9)
              </span>
              <span className="text-xs text-slate-400 font-mono">HD Widescreen</span>
            </div>
            <h3 className="text-base font-bold text-slate-100">
              Промо-баннер для шапки описания на CurseForge / Modrinth
            </h3>
            <p className="text-xs text-slate-400">
              Вы можете вставить эту картинку в самое начало описания проекта на странице CurseForge.
            </p>
          </div>

          <div className="relative w-full aspect-video rounded-xl overflow-hidden border border-slate-700 shadow-2xl bg-slate-950">
            <img
              src={modBannerUrl}
              alt="Seven Deadly Sins Mod Promotional Banner"
              referrerPolicy="no-referrer"
              className="w-full h-full object-cover"
            />
          </div>

          <button
            onClick={() => downloadImage(modBannerUrl, 'sevendeadlysins-curseforge-banner.jpg')}
            className="w-full py-2.5 px-4 rounded-xl bg-slate-800 hover:bg-slate-700 text-slate-100 border border-slate-700 font-semibold text-xs transition-colors flex items-center justify-center gap-2"
          >
            <Download className="w-4 h-4 text-amber-400" />
            Скачать Промо-Баннер для Описания (.jpg)
          </button>
        </div>
      </div>

      {/* CurseForge Short Summary & Metadata */}
      <div className="p-6 rounded-2xl bg-[#0F172A] border border-slate-800 space-y-4">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <div className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-amber-400" />
            <h3 className="text-base font-bold text-slate-100">
              Поля при создании проекта на CurseForge (Name, Summary, Categories)
            </h3>
          </div>
          <span className="text-xs text-slate-400 font-mono">
            Категории: Magic • RPG • Adventure and RPG • Armor, Tools, and Weapons
          </span>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono text-amber-400">Short Summary (English — для модерации CurseForge)</span>
              <button
                onClick={() => copyText('summary-en', CURSEFORGE_SHORT_SUMMARY_EN)}
                className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 flex items-center gap-1"
              >
                {copiedKey === 'summary-en' ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                {copiedKey === 'summary-en' ? 'Скопировано' : 'Копировать'}
              </button>
            </div>
            <p className="text-xs text-slate-300 font-mono leading-relaxed">{CURSEFORGE_SHORT_SUMMARY_EN}</p>
          </div>

          <div className="p-4 rounded-xl bg-slate-900 border border-slate-800 space-y-2">
            <div className="flex items-center justify-between">
              <span className="text-xs font-mono text-emerald-400">Краткое описание (Русский)</span>
              <button
                onClick={() => copyText('summary-ru', CURSEFORGE_SHORT_SUMMARY_RU)}
                className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-xs text-slate-200 flex items-center gap-1"
              >
                {copiedKey === 'summary-ru' ? <Check className="w-3 h-3 text-emerald-400" /> : <Copy className="w-3 h-3" />}
                {copiedKey === 'summary-ru' ? 'Скопировано' : 'Копировать'}
              </button>
            </div>
            <p className="text-xs text-slate-300 font-mono leading-relaxed">{CURSEFORGE_SHORT_SUMMARY_RU}</p>
          </div>
        </div>
      </div>

      {/* Full Markdown Description with Language Switcher */}
      <div className="rounded-2xl bg-[#0F172A] border border-slate-800 overflow-hidden">
        <div className="flex flex-wrap items-center justify-between gap-4 px-6 py-4 bg-slate-900/90 border-b border-slate-800">
          <div className="flex items-center gap-3">
            <Globe className="w-4 h-4 text-amber-400" />
            <div>
              <h3 className="text-sm font-bold text-slate-100">
                Готовое полное описание страницы мода (Markdown для CurseForge / Modrinth)
              </h3>
              <p className="text-xs text-slate-400">
                Нажмите «Копировать описание» и вставьте прямо в редактор описания на CurseForge
              </p>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <div className="flex items-center bg-slate-950 p-1 rounded-lg border border-slate-800 text-xs">
              <button
                onClick={() => setDescLang('en')}
                className={`px-3 py-1 rounded-md font-medium transition-colors ${
                  descLang === 'en' ? 'bg-amber-500 text-slate-950 font-semibold' : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                English (Обязательно для CurseForge)
              </button>
              <button
                onClick={() => setDescLang('ru')}
                className={`px-3 py-1 rounded-md font-medium transition-colors ${
                  descLang === 'ru' ? 'bg-amber-500 text-slate-950 font-semibold' : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                Русская версия
              </button>
            </div>

            <button
              onClick={() =>
                copyText(
                  'full-desc',
                  descLang === 'en' ? CURSEFORGE_MARKDOWN_EN : CURSEFORGE_MARKDOWN_RU
                )
              }
              className="px-4 py-2 rounded-lg bg-amber-500 hover:bg-amber-400 text-slate-950 font-semibold text-xs transition-colors flex items-center gap-1.5"
            >
              {copiedKey === 'full-desc' ? (
                <>
                  <Check className="w-3.5 h-3.5" />
                  Скопировано!
                </>
              ) : (
                <>
                  <Copy className="w-3.5 h-3.5" />
                  Копировать описание ({descLang.toUpperCase()})
                </>
              )}
            </button>
          </div>
        </div>

        <pre className="p-6 text-xs font-mono text-slate-200 overflow-x-auto leading-relaxed max-h-[560px] overflow-y-auto whitespace-pre-wrap">
          {descLang === 'en' ? CURSEFORGE_MARKDOWN_EN : CURSEFORGE_MARKDOWN_RU}
        </pre>
      </div>
    </div>
  );
};
