import React, { useState } from 'react';
import { ALL_MOD_FILES, downloadModProjectZip } from './data/allModFiles';
import { SINS_SPECS } from './data/modTypes';
import { SinsHudSimulator } from './components/SinsHudSimulator';
import { CurseForgeShowcase } from './components/CurseForgeShowcase';
import {
  Copy,
  Check,
  Download,
  FileCode,
  FolderTree,
  Layers,
  Terminal,
  Search,
  GitBranch,
  Flame,
  Zap,
  ShieldCheck,
  Sparkles,
} from 'lucide-react';

export default function App() {
  const [activeView, setActiveView] = useState<'all-ordered' | 'explorer' | 'simulator' | 'curseforge'>('curseforge');
  const [selectedFileId, setSelectedFileId] = useState<string>(ALL_MOD_FILES[0].id);
  const [copiedId, setCopiedId] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [isDownloadingZip, setIsDownloadingZip] = useState<boolean>(false);

  const handleCopy = (id: string, text: string) => {
    navigator.clipboard.writeText(text);
    setCopiedId(id);
    setTimeout(() => {
      setCopiedId((prev) => (prev === id ? null : prev));
    }, 2000);
  };

  const handleCopyAllOrdered = () => {
    const combined = ALL_MOD_FILES.map(
      (f) =>
        `// ============================================================================\n` +
        `// РАЗДЕЛ: ${f.sectionTitle}\n` +
        `// ПУТЬ: ${f.path}\n` +
        `// ============================================================================\n\n` +
        f.content
    ).join('\n\n\n');
    handleCopy('all-project-code', combined);
  };

  const handleDownloadZip = async () => {
    setIsDownloadingZip(true);
    try {
      await downloadModProjectZip();
    } finally {
      setIsDownloadingZip(false);
    }
  };

  const workflowFile = ALL_MOD_FILES.find((f) => f.id === 'github-workflow-build');

  const filteredFiles = ALL_MOD_FILES.filter(
    (f) =>
      f.path.toLowerCase().includes(searchQuery.toLowerCase()) ||
      f.filename.toLowerCase().includes(searchQuery.toLowerCase()) ||
      f.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
      f.content.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const selectedFile = ALL_MOD_FILES.find((f) => f.id === selectedFileId) || ALL_MOD_FILES[0];

  const sections = [
    {
      order: 1,
      title: '1. Конфигурационные файлы сборки Minecraft 1.20.1 (Forge 47.3.0 / Java 17) и GitHub Actions CI/CD',
      subtitle:
        'ForgeGradle 6.0, официальные маппинги 1.20.1, дескриптор META-INF/mods.toml с модами Cisco\'s RPG [Dragonfyre] и автосборка reobfJar в GitHub Actions на JDK 17.',
    },
    {
      order: 2,
      title: '2. Главный класс мода, TPS-Кэш Данных Игрока, Запретный Плод и Loot Modifier для данжей Dragonfyre (7 файлов)',
      subtitle:
        'Мгновенный O(1) кэш PlayerSinsData с сохранением в Player.PERSISTED_NBT_TAG и генерация Запретного Плода в Древнем Городе, Бастионах, логовах Драконов Ice & Fire и данжах Cataclysm.',
    },
    {
      order: 3,
      title: '3. Класс данных PlayerSinsData (Java 17) и Сетевой Канал SimpleChannel (5 файлов)',
      subtitle:
        'Совместимая с Java 17 сериализация CompoundTag, Ранг Души Dragonfyre, флаг dirty для экономии 90% сетевых пакетов и канал NetworkRegistry.newSimpleChannel.',
    },
    {
      order: 4,
      title: '4. Движок 7 Грехов, Оптимизация TPS и Модуль интеграции с Cisco\'s Fantasy Medieval RPG [Dragonfyre] (10 файлов)',
      subtitle:
        'Гибридный % урон от макс. HP для боссов 2000+ HP, динамическая прокачка Crit/Armor Shred (Apotheosis) и Spell Power (Iron\'s Spells), кража Дыхания Драконов Ice & Fire и атак боссов Cataclysm.',
    },
  ] as const;

  return (
    <div className="min-h-screen flex flex-col bg-[#0B0F17] text-slate-100">
      {/* Top Bar */}
      <header className="sticky top-0 z-30 flex items-center justify-between px-6 py-3.5 border-b border-slate-800/90 bg-[#0B0F17]/95 backdrop-blur">
        <a
          href="#top"
          onClick={(e) => {
            e.preventDefault();
            setActiveView('all-ordered');
          }}
          className="text-lg font-bold tracking-tight text-slate-100 font-display whitespace-nowrap"
        >
          Seven Deadly Sins <span className="text-amber-400 text-sm font-mono">[1.20.1 Dragonfyre]</span>
        </a>

        <nav className="hidden md:flex items-center gap-6 text-sm font-medium text-slate-400">
          <button
            onClick={() => setActiveView('curseforge')}
            className={`hover:text-amber-300 transition-colors whitespace-nowrap flex items-center gap-1.5 ${
              activeView === 'curseforge' ? 'text-amber-400 font-semibold underline underline-offset-8' : 'text-amber-300/80'
            }`}
          >
            <Sparkles className="w-3.5 h-3.5" />
            CurseForge (Аватар + Описание)
          </button>
          <button
            onClick={() => setActiveView('all-ordered')}
            className={`hover:text-slate-100 transition-colors whitespace-nowrap ${
              activeView === 'all-ordered' ? 'text-amber-400 underline underline-offset-8' : ''
            }`}
          >
            Код 1.20.1 ({ALL_MOD_FILES.length} файла)
          </button>
          <button
            onClick={() => setActiveView('explorer')}
            className={`hover:text-slate-100 transition-colors whitespace-nowrap ${
              activeView === 'explorer' ? 'text-amber-400 underline underline-offset-8' : ''
            }`}
          >
            IDE Проводник
          </button>
          <button
            onClick={() => setActiveView('simulator')}
            className={`hover:text-slate-100 transition-colors whitespace-nowrap ${
              activeView === 'simulator' ? 'text-amber-400 underline underline-offset-8' : ''
            }`}
          >
            Симулятор Dragonfyre
          </button>
          <a
            href="#github-guide"
            onClick={() => setActiveView('all-ordered')}
            className="hover:text-slate-100 transition-colors whitespace-nowrap"
          >
            Сборка через GitHub
          </a>
        </nav>

        <div className="flex items-center gap-2.5">
          {workflowFile && (
            <button
              onClick={() => handleCopy('header-workflow', workflowFile.content)}
              className="px-3.5 py-2 text-xs font-medium text-slate-200 bg-slate-800 hover:bg-slate-700 border border-slate-700 rounded-lg transition-colors flex items-center gap-1.5 whitespace-nowrap"
            >
              {copiedId === 'header-workflow' ? (
                <>
                  <Check className="w-3.5 h-3.5 text-emerald-400" />
                  build.yml скопирован
                </>
              ) : (
                <>
                  <GitBranch className="w-3.5 h-3.5 text-amber-400" />
                  Копировать build.yml (JDK 17)
                </>
              )}
            </button>
          )}

          <button
            onClick={handleDownloadZip}
            disabled={isDownloadingZip}
            className="px-4 py-2 text-xs font-semibold text-slate-950 bg-amber-500 hover:bg-amber-400 rounded-lg transition-colors flex items-center gap-1.5 whitespace-nowrap"
          >
            <Download className="w-3.5 h-3.5" />
            {isDownloadingZip ? 'Упаковка...' : 'Скачать 1.20.1 Dragonfyre (.zip)'}
          </button>
        </div>
      </header>

      {/* Main Content Container */}
      <main id="top" className="flex-1 max-w-[1400px] w-full mx-auto px-6 py-8 space-y-10">
        {/* Hero Architectural Summary + Dragonfyre Optimizations */}
        <section className="border-b border-slate-800/90 pb-8 space-y-6">
          <div className="flex flex-col lg:flex-row lg:items-end justify-between gap-6">
            <div className="space-y-3 max-w-3xl">
              <div className="flex flex-wrap items-center gap-2 text-xs text-slate-400 font-mono">
                <span className="text-amber-400">Cisco&apos;s Fantasy Medieval RPG [Dragonfyre]</span>
                <span aria-hidden="true">·</span>
                <span>Minecraft 1.20.1 (Forge 47.3.0)</span>
                <span aria-hidden="true">·</span>
                <span>Java 17 (Temurin)</span>
                <span aria-hidden="true">·</span>
                <span>TPS &amp; Embeddium Optimized</span>
              </div>
              <h1 className="text-2xl sm:text-3xl font-bold text-slate-100 tracking-tight leading-tight">
                Seven Deadly Sins для Minecraft 1.20.1 — Оптимизировано под модпак Cisco&apos;s RPG [Dragonfyre]
              </h1>
              <p className="text-sm text-slate-400 leading-relaxed">
                Полностью пересозданная кодовая база под <code className="text-amber-300 font-mono">Minecraft 1.20.1 (Forge 47.3.0, Java 17)</code>.
                Добавлена глубокая интеграция с ключевыми модами сборки <strong className="text-slate-200">Cisco&apos;s Fantasy Medieval RPG [Dragonfyre]</strong>:{' '}
                <code className="text-emerald-400 font-mono">Apotheosis (AttributesLib)</code>,{' '}
                <code className="text-emerald-400 font-mono">Iron&apos;s Spells &apos;n Spellbooks</code>,{' '}
                <code className="text-emerald-400 font-mono">Ice and Fire: Dragons</code>,{' '}
                <code className="text-emerald-400 font-mono">L_Ender&apos;s Cataclysm</code>,{' '}
                <code className="text-emerald-400 font-mono">Simply Swords</code> и{' '}
                <code className="text-emerald-400 font-mono">L2Hostility</code>.
              </p>
              <div className="p-3.5 rounded-lg bg-emerald-950/30 border border-emerald-500/30 text-xs text-emerald-200 flex items-start gap-2.5">
                <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
                <div>
                  <span className="font-semibold text-emerald-300">Исправлена ошибка загрузки Forge 1.20.1 («не удалось загрузить правильную информацию о наборе ресурсов»):</span>{' '}
                  В корень ресурсов добавлен и жестко зафиксирован <code className="text-amber-300 font-mono">src/main/resources/pack.mcmeta</code> с <code className="text-amber-300 font-mono">pack_format: 15</code> и объектом <code className="text-amber-300 font-mono">description.text</code>, удалён конфликтующий <code className="font-mono">neoforge.mods.toml</code>, в <code className="font-mono">mods.toml</code> выставлены универсальные диапазоны <code className="font-mono">[0,)</code> для 3C Beta, а вызовы <code className="font-mono">Holder.Reference&lt;SoundEvent&gt;</code> приведены к API 1.20.1.
                </div>
              </div>
            </div>

            {/* Mode Switcher Segmented Controls */}
            <div className="flex flex-wrap items-center gap-1 p-1 bg-slate-900 border border-slate-800 rounded-lg self-start lg:self-end">
              <button
                onClick={() => setActiveView('curseforge')}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors flex items-center gap-1.5 whitespace-nowrap ${
                  activeView === 'curseforge'
                    ? 'bg-amber-500 text-slate-950 font-semibold'
                    : 'text-amber-300 hover:text-slate-100'
                }`}
              >
                <Sparkles className="w-3.5 h-3.5" />
                CurseForge Кит (Фото + Описание)
              </button>
              <button
                onClick={() => setActiveView('all-ordered')}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors flex items-center gap-1.5 whitespace-nowrap ${
                  activeView === 'all-ordered'
                    ? 'bg-amber-500 text-slate-950 font-semibold'
                    : 'text-slate-400 hover:text-slate-100'
                }`}
              >
                <Layers className="w-3.5 h-3.5" />
                Все файлы по порядку
              </button>
              <button
                onClick={() => setActiveView('explorer')}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors flex items-center gap-1.5 whitespace-nowrap ${
                  activeView === 'explorer'
                    ? 'bg-amber-500 text-slate-950 font-semibold'
                    : 'text-slate-400 hover:text-slate-100'
                }`}
              >
                <FolderTree className="w-3.5 h-3.5" />
                IDE Проводник ({ALL_MOD_FILES.length})
              </button>
              <button
                onClick={() => setActiveView('simulator')}
                className={`px-3 py-1.5 text-xs font-medium rounded-md transition-colors flex items-center gap-1.5 whitespace-nowrap ${
                  activeView === 'simulator'
                    ? 'bg-amber-500 text-slate-950 font-semibold'
                    : 'text-slate-400 hover:text-slate-100'
                }`}
              >
                <Terminal className="w-3.5 h-3.5" />
                Тест HUD и Механик
              </button>
            </div>
          </div>

          {/* 3-Column Dragonfyre Optimization Highlights */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
            <div className="p-4 rounded-xl bg-[#0F172A] border border-slate-800 space-y-2">
              <div className="flex items-center gap-2 font-semibold text-amber-400">
                <Flame className="w-4 h-4" />
                Интеграция с Драконами, Боссами и Apotheosis
              </div>
              <p className="text-slate-400 leading-relaxed">
                <strong>Зависть</strong> крадёт Огненное/Ледяное/Грозовое дыхание драконов <code className="text-slate-200">Ice &amp; Fire</code> и навыки боссов <code className="text-slate-200">Cataclysm</code> (Игнис, Маледиктус, Предвестник).{' '}
                <strong>Гнев</strong> напрямую усиливает <code className="text-amber-300">Crit Chance</code>, <code className="text-amber-300">Crit Damage</code> и <code className="text-amber-300">Armor Shred</code> из <code className="text-slate-200">Apotheosis</code>, а <strong>Алчность</strong> копирует мифическое оружие со всеми сокетами и самоцветами.
              </p>
            </div>

            <div className="p-4 rounded-xl bg-[#0F172A] border border-slate-800 space-y-2">
              <div className="flex items-center gap-2 font-semibold text-emerald-400">
                <Zap className="w-4 h-4" />
                Оптимизация TPS и FPS под 250+ модов
              </div>
              <p className="text-slate-400 leading-relaxed">
                Полностью удалён глобальный <code className="text-slate-200">LivingTickEvent</code> (который вызывал бы просадки TPS в данжах с сотнями мобов Dragonfyre). Данные игрока кэшируются в памяти <code className="text-emerald-300">O(1) ConcurrentHashMap</code>, а сетевые пакеты <code className="text-slate-200">SimpleChannel</code> отправляются только при флаге <code className="text-emerald-300">isDirty()</code>.
              </p>
            </div>

            <div className="p-4 rounded-xl bg-[#0F172A] border border-slate-800 space-y-2">
              <div className="flex items-center gap-2 font-semibold text-sky-400">
                <ShieldCheck className="w-4 h-4" />
                Баланс против L2Hostility и Боссов 2000+ HP
              </div>
              <p className="text-slate-400 leading-relaxed">
                Все атакующие навыки используют гибридную формулу <code className="text-sky-300">База + Атака Игрока + % от Макс. HP цели</code>. Зависть срывает регенерацию и баффы с адаптивных мобов <code className="text-slate-200">L2Hostility</code>, а клавиши <code className="text-slate-200">R, V, X</code> изолированы через <code className="text-sky-300">KeyConflictContext.IN_GAME</code>.
              </p>
            </div>
          </div>

          {/* Step-by-Step GitHub Actions Guide */}
          <div id="github-guide" className="p-5 rounded-xl bg-[#0F172A] border border-amber-500/30 space-y-4">
            <div className="flex flex-wrap items-center justify-between gap-3 border-b border-slate-800 pb-3">
              <div>
                <h2 className="text-sm font-semibold text-amber-400">
                  Инструкция: как собрать готовый .jar под Minecraft 1.20.1 (Cisco&apos;s RPG Dragonfyre) на GitHub
                </h2>
                <p className="text-xs text-slate-400 mt-0.5">
                  Сборка выполняется на <strong className="text-slate-200">JDK 17 + ForgeGradle 6.0 (Forge 47.3.0)</strong> с автоматической обфускацией <code className="text-slate-200 font-mono">reobfJar</code> для прямой установки в папку <code className="text-amber-300 font-mono">mods/</code> модпака.
                </p>
              </div>
              <button
                onClick={handleDownloadZip}
                disabled={isDownloadingZip}
                className="px-3.5 py-2 rounded-lg bg-amber-500 hover:bg-amber-400 text-slate-950 text-xs font-semibold flex items-center gap-1.5 transition-colors whitespace-nowrap"
              >
                <Download className="w-3.5 h-3.5" />
                1. Скачать sevendeadlysins-1.20.1-dragonfyre-github-ready.zip
              </button>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-xs">
              <div className="p-3.5 rounded-lg bg-[#0B0F17] border border-slate-800 space-y-1.5">
                <div className="font-semibold text-slate-200">Шаг 1. Загрузите файлы в репозиторий</div>
                <p className="text-slate-400 leading-relaxed">
                  Распакуйте скачанный <code className="text-amber-300 font-mono">.zip</code> архив. В репозитории на GitHub нажмите{' '}
                  <strong className="text-slate-200">Add file → Upload files</strong>, перетащите все папки и файлы (<code className="text-slate-300 font-mono">build.gradle</code>, <code className="text-slate-300 font-mono">gradle.properties</code>, <code className="text-slate-300 font-mono">settings.gradle</code> и <code className="text-slate-300 font-mono">src</code>) и нажмите <strong className="text-slate-200">Commit changes</strong>.
                </p>
              </div>

              <div className="p-3.5 rounded-lg bg-[#0B0F17] border border-slate-800 space-y-1.5">
                <div className="font-semibold text-slate-200">Шаг 2. Проверьте запуск GitHub Actions (JDK 17)</div>
                <p className="text-slate-400 leading-relaxed">
                  Откройте вкладку <strong className="text-slate-200">Actions</strong> на GitHub. Если скрытая папка <code className="text-slate-300 font-mono">.github</code> не была перетащена, нажмите <strong className="text-slate-200">set up a workflow yourself</strong> и вставьте скопированный{' '}
                  <button
                    onClick={() => workflowFile && handleCopy('inline-wf', workflowFile.content)}
                    className="text-amber-400 underline hover:text-amber-300 font-mono"
                  >
                    {copiedId === 'inline-wf' ? 'Скопировано!' : '.github/workflows/build.yml'}
                  </button>
                  .
                </p>
              </div>

              <div className="p-3.5 rounded-lg bg-[#0B0F17] border border-slate-800 space-y-1.5">
                <div className="font-semibold text-slate-200">Шаг 3. Поместите .jar в папку mods/ сборки Dragonfyre</div>
                <p className="text-slate-400 leading-relaxed">
                  После завершения сборки скачайте из блока <strong className="text-emerald-400">Artifacts</strong> архив{' '}
                  <code className="text-emerald-300 font-mono">sevendeadlysins-1.2.0-forge-1.20.1-dragonfyre-jar</code> и скопируйте готовый <code className="text-slate-300 font-mono">.jar</code> в папку <code className="text-amber-300 font-mono">mods/</code> вашего клиента и сервера Cisco&apos;s RPG.
                </p>
              </div>
            </div>
          </div>

          {/* Directory Structure Overview */}
          <div className="p-4 rounded-xl bg-[#0F172A] border border-slate-800/90">
            <div className="flex flex-wrap items-center justify-between gap-2 pb-3 mb-3 border-b border-slate-800">
              <span className="text-xs font-semibold text-slate-200">
                Файловая структура архива sevendeadlysins-1.20.1-dragonfyre-github-ready.zip (Forge 1.20.1 / Java 17)
              </span>
              <button
                onClick={handleCopyAllOrdered}
                className="px-2.5 py-1 rounded bg-slate-800 hover:bg-slate-700 text-xs font-mono text-slate-200 border border-slate-700 flex items-center gap-1.5 transition-colors whitespace-nowrap"
              >
                {copiedId === 'all-project-code' ? (
                  <>
                    <Check className="w-3.5 h-3.5 text-emerald-400" />
                    Код скопирован
                  </>
                ) : (
                  <>
                    <Copy className="w-3.5 h-3.5" />
                    Копировать все файлы одним списком
                  </>
                )}
              </button>
            </div>
            <pre className="text-xs font-mono text-slate-300 overflow-x-auto leading-relaxed">
{`sevendeadlysins-1.20.1-dragonfyre-github-ready/
├── .github/workflows/build.yml                                               # Автосборка на GitHub Actions (JDK 17 + Gradle 8.8 + reobfJar)
├── build.gradle                                                              # ForgeGradle 6.0, Minecraft 1.20.1 (Forge 47.3.0), Java 17
├── gradle.properties                                                         # Оптимизированные флаги G1GC JVM для Dragonfyre
├── settings.gradle                                                           # Репозиторий MinecraftForge и Foojay Toolchain
└── src/main/
    ├── java/com/sevendeadlysins/
    │   ├── SevenDeadlySinsMod.java                                           # Главный класс @Mod("sevendeadlysins") для 1.20.1
    │   ├── registry/
    │   │   ├── ModAttachmentTypes.java                                       # O(1) Кэш данных игрока + Player.PERSISTED_NBT_TAG
    │   │   ├── ModItems.java                                                 # Регистрация ForbiddenFruitItem (ForgeRegistries.ITEMS)
    │   │   ├── ModEntities.java                                              # Регистрация SoulOrbEntity (ForgeRegistries.ENTITY_TYPES)
    │   │   └── ModLootModifiers.java                                         # Регистрация GLOBAL_LOOT_MODIFIER_SERIALIZERS
    │   ├── item/
    │   │   └── ForbiddenFruitItem.java                                       # Запретный Плод Dragonfyre (+50 маны, +1 Ранг Души, сброс КД)
    │   ├── loot/
    │   │   └── AddLootModifier.java                                          # Лут в Древнем Городе, Бастионах, логовах Ice&Fire и Cataclysm
    │   ├── data/
    │   │   └── PlayerSinsData.java                                           # INBTSerializable<CompoundTag>, dirty-флаг и Ранг Dragonfyre
    │   ├── network/
    │   │   ├── CastSinPayload.java                                           # Пакет C2S применения способности / смены подрежима
    │   │   ├── SyncSinsDataPayload.java                                      # Пакет S2C синхронизации NBT на клиент
    │   │   ├── SelectSinPayload.java                                         # Пакет C2S выбора греха из радиального меню
    │   │   └── ModNetwork.java                                               # Канал NetworkRegistry.newSimpleChannel (1.20.1)
    │   ├── ability/
    │   │   └── SinsAbilityEngine.java                                        # Логика 7 Грехов с гибридным % Max HP уроном для боссов
    │   ├── event/
    │   │   └── SinsCommonEvents.java                                         # TPS-оптимизированные события (без глобального LivingTickEvent)
    │   ├── entity/
    │   │   └── SoulOrbEntity.java                                            # Сущность сферы души Чревоугодия (NetworkHooks 1.20.1)
    │   ├── client/
    │   │   ├── ModKeyBindings.java                                           # Клавиши R, V, X с KeyConflictContext.IN_GAME
    │   │   ├── SoulOrbRenderer.java                                          # Рендерер сферы души (Matrix4f / Совместим с Embeddium & Oculus)
    │   │   ├── SinsHudOverlay.java                                           # Отрисовка IGuiOverlay (Мана, Гнев, Ранг Души Dragonfyre)
    │   │   ├── RadialMenuScreen.java                                         # Радиальное меню выбора греха
    │   │   ├── GreedCatalogScreen.java                                       # Каталог Алчности с репликацией аффиксов Apotheosis
    │   │   └── EnvySelectionScreen.java                                      # Арсенал Зависти (Дыхание Драконов Ice&Fire, Боссы Cataclysm)
    │   └── compat/
    │       └── CompatManager.java                                            # Мост к Apotheosis, Iron's Spells, Ice&Fire, Cataclysm, L2Hostility
    └── resources/
        ├── pack.mcmeta                                                       # pack_format: 15 (Minecraft 1.20.1)
        ├── META-INF/mods.toml                                                # Дескриптор Forge 47+ с опциональными модами Cisco's RPG
        ├── assets/sevendeadlysins/
        │   ├── lang/ (ru_ru.json, en_us.json)
        │   ├── models/item/forbidden_fruit.json
        │   └── textures/item/forbidden_fruit.png
        └── data/
            ├── forge/loot_modifiers/global_loot_modifiers.json
            └── sevendeadlysins/loot_modifiers/forbidden_fruit_ancient_city.json`}
            </pre>
          </div>
        </section>

        {/* Interactive HUD Simulator View */}
        {activeView === 'simulator' && (
          <section className="space-y-4">
            <div>
              <h2 className="text-xl font-bold text-slate-100">
                Интерактивный стенд проверки механик 1.20.1 и интеграции с Cisco&apos;s RPG [Dragonfyre]
              </h2>
              <p className="text-xs text-slate-400 mt-1">
                Протестируйте работу клавиш <code className="text-amber-400">R</code>,{' '}
                <code className="text-amber-400">V</code>, <code className="text-amber-400">X</code>, масштабирование
                атрибутов <code className="text-emerald-400">Apotheosis / Iron&apos;s Spells</code> и кражу Дыхания Драконов.
              </p>
            </div>
            <SinsHudSimulator />
          </section>
        )}

        {/* Explorer View (Split IDE) */}
        {activeView === 'explorer' && (
          <section className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
            <div className="lg:col-span-4 border border-slate-800 bg-[#0F172A] rounded-xl p-4 space-y-3">
              <div className="relative">
                <Search className="w-4 h-4 text-slate-500 absolute left-3 top-1/2 -translate-y-1/2" />
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="Поиск по классу или файлу..."
                  className="w-full pl-9 pr-3 py-2 bg-[#0B0F17] border border-slate-800 rounded-lg text-xs text-slate-200 placeholder:text-slate-500 focus:outline-none focus:border-amber-500/60"
                />
              </div>

              <div className="space-y-1 max-h-[680px] overflow-y-auto pr-1">
                {filteredFiles.map((file) => {
                  const active = file.id === selectedFile.id;
                  return (
                    <button
                      key={file.id}
                      onClick={() => setSelectedFileId(file.id)}
                      className={`w-full text-left p-2.5 rounded-lg transition-colors flex items-start gap-2.5 ${
                        active
                          ? 'bg-amber-500/15 border border-amber-500/40 text-amber-300'
                          : 'hover:bg-slate-900 text-slate-300 border border-transparent'
                      }`}
                    >
                      <FileCode className="w-4 h-4 shrink-0 mt-0.5 text-amber-400" />
                      <div className="min-w-0">
                        <div className="text-xs font-mono font-semibold truncate">{file.filename}</div>
                        <div className="text-[11px] text-slate-500 font-mono truncate">{file.path}</div>
                      </div>
                    </button>
                  );
                })}
              </div>
            </div>

            <div className="lg:col-span-8 border border-slate-800 bg-[#0F172A] rounded-xl overflow-hidden">
              <div className="flex flex-wrap items-center justify-between gap-3 px-5 py-3.5 border-b border-slate-800 bg-[#0B0F17]/60">
                <div>
                  <div className="text-xs font-mono font-semibold text-amber-400">{selectedFile.path}</div>
                  <div className="text-xs text-slate-400 mt-0.5">{selectedFile.description}</div>
                </div>
                <button
                  onClick={() => handleCopy(selectedFile.id, selectedFile.content)}
                  className="px-3 py-1.5 rounded bg-slate-800 hover:bg-slate-700 text-xs font-mono text-slate-200 border border-slate-700 flex items-center gap-1.5 transition-colors whitespace-nowrap"
                >
                  {copiedId === selectedFile.id ? (
                    <>
                      <Check className="w-3.5 h-3.5 text-emerald-400" />
                      Скопировано
                    </>
                  ) : (
                    <>
                      <Copy className="w-3.5 h-3.5" />
                      Копировать файл
                    </>
                  )}
                </button>
              </div>
              <pre className="p-5 text-xs font-mono text-slate-200 overflow-x-auto leading-relaxed max-h-[720px] overflow-y-auto">
                <code>{selectedFile.content}</code>
              </pre>
            </div>
          </section>
        )}

        {/* CurseForge Publishing Kit View */}
        {activeView === 'curseforge' && <CurseForgeShowcase />}

        {/* Default View: All 4 Ordered Sections Without Truncation */}
        {activeView === 'all-ordered' && (
          <div className="space-y-12">
            {/* Compact Interactive Preview Banner */}
            <section className="space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-2">
                <div>
                  <h2 className="text-lg font-bold text-slate-100">
                    Интерактивная панель тестирования HUD 1.20.1 и интеграции с Cisco&apos;s RPG [Dragonfyre]
                  </h2>
                  <p className="text-xs text-slate-400">
                    Проверьте работу всех 7 грехов, атрибутов Apotheosis, свитков Iron&apos;s Spells и Дыхания Драконов перед сборкой .jar.
                  </p>
                </div>
              </div>
              <SinsHudSimulator />
            </section>

            {/* Ordered Code Output: Sections 1, 2, 3, 4 */}
            {sections.map((sec) => {
              const sectionFiles = ALL_MOD_FILES.filter((f) => f.sectionOrder === sec.order);
              return (
                <section key={sec.order} className="space-y-5">
                  <div className="border-b border-slate-800 pb-3">
                    <h2 className="text-xl font-bold text-amber-400">{sec.title}</h2>
                    <p className="text-xs text-slate-400 mt-1">{sec.subtitle}</p>
                  </div>

                  <div className="space-y-6">
                    {sectionFiles.map((file) => (
                      <div
                        key={file.id}
                        className="border border-slate-800 bg-[#0F172A] rounded-xl overflow-hidden"
                      >
                        <div className="flex flex-wrap items-center justify-between gap-3 px-5 py-3 border-b border-slate-800 bg-[#0B0F17]/70">
                          <div className="min-w-0">
                            <div className="flex items-center gap-2 text-xs font-mono">
                              <span className="font-semibold text-slate-100">{file.filename}</span>
                              <span aria-hidden="true" className="text-slate-600">
                                ·
                              </span>
                              <span className="text-amber-400/90 truncate">{file.path}</span>
                            </div>
                            <p className="text-xs text-slate-400 mt-0.5">{file.description}</p>
                          </div>

                          <button
                            onClick={() => handleCopy(file.id, file.content)}
                            className="px-3 py-1.5 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-mono text-slate-200 border border-slate-700 flex items-center gap-1.5 transition-colors shrink-0 whitespace-nowrap"
                          >
                            {copiedId === file.id ? (
                              <>
                                <Check className="w-3.5 h-3.5 text-emerald-400" />
                                Скопировано
                              </>
                            ) : (
                              <>
                                <Copy className="w-3.5 h-3.5" />
                                Копировать код
                              </>
                            )}
                          </button>
                        </div>

                        <pre className="p-5 text-xs font-mono text-slate-200 overflow-x-auto leading-relaxed">
                          <code>{file.content}</code>
                        </pre>
                      </div>
                    ))}
                  </div>
                </section>
              );
            })}
          </div>
        )}

        {/* Reference Matrix of the 7 Deadly Sins */}
        <section id="sins-matrix" className="pt-6 border-t border-slate-800 space-y-4">
          <div>
            <h2 className="text-lg font-bold text-slate-100">
              Сводная таблица механик Семи Смертных Грехов под Cisco&apos;s RPG [Dragonfyre] (1.20.1)
            </h2>
            <p className="text-xs text-slate-400 mt-0.5">
              Все параметры соответствуют серверной реализации в <code className="font-mono text-amber-400">SinsAbilityEngine.java</code> и <code className="font-mono text-amber-400">CompatManager.java</code>.
            </p>
          </div>

          <div className="overflow-x-auto border border-slate-800 rounded-xl bg-[#0F172A]">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="border-b border-slate-800 text-slate-400 bg-[#0B0F17]/60">
                  <th className="py-3 px-4 font-semibold">Грех (ID)</th>
                  <th className="py-3 px-4 font-semibold">Подрежимы (Клавиша X)</th>
                  <th className="py-3 px-4 font-semibold text-right">Мана</th>
                  <th className="py-3 px-4 font-semibold text-right">Кулдаун (Тики)</th>
                  <th className="py-3 px-4 font-semibold">Оптимизация и Синергия с Dragonfyre 1.20.1</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800/70">
                {SINS_SPECS.map((sin) => (
                  <tr key={sin.id} className="hover:bg-slate-900/50 transition-colors">
                    <td className="py-3 px-4 font-semibold text-amber-400 whitespace-nowrap">
                      {sin.id}. {sin.titleRu} ({sin.titleEn})
                    </td>
                    <td className="py-3 px-4 font-mono text-slate-300">
                      {sin.modes.join(' / ')}
                    </td>
                    <td className="py-3 px-4 font-mono tabular-nums text-right text-blue-400 whitespace-nowrap">
                      {sin.manaCost} ед.
                    </td>
                    <td className="py-3 px-4 font-mono tabular-nums text-right text-slate-300 whitespace-nowrap">
                      {sin.cooldownTicks}L
                    </td>
                    <td className="py-3 px-4 text-slate-300 leading-relaxed">{sin.summary}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </section>
      </main>

      {/* Clean Minimal Footer */}
      <footer className="border-t border-slate-800/80 py-5 px-6 text-xs text-slate-500">
        <div className="max-w-[1400px] mx-auto flex flex-wrap items-center justify-between gap-4">
          <span>Seven Deadly Sins [Dragonfyre Edition] · Minecraft 1.20.1 · Forge 47.3.0 · Java 17</span>
          <div className="flex items-center gap-4">
            <button
              onClick={handleDownloadZip}
              className="text-amber-400 hover:text-amber-300 font-medium transition-colors"
            >
              Скачать 1.20.1 Dragonfyre (.zip)
            </button>
            <span aria-hidden="true">·</span>
            <button
              onClick={handleCopyAllOrdered}
              className="text-slate-400 hover:text-amber-400 transition-colors"
            >
              Скопировать весь код
            </button>
          </div>
        </div>
      </footer>
    </div>
  );
}
