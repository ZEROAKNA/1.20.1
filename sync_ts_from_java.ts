import fs from 'fs';

const buildGradle = fs.readFileSync('build.gradle', 'utf8');
const gradleProperties = fs.readFileSync('gradle.properties', 'utf8');
const settingsGradle = fs.readFileSync('settings.gradle', 'utf8');
const modsToml = fs.readFileSync('src/main/resources/META-INF/mods.toml', 'utf8');
const packMcmeta = fs.readFileSync('src/main/resources/pack.mcmeta', 'utf8');
const workflowYml = fs.readFileSync('.github/workflows/build.yml', 'utf8');

const mainModJava = fs.readFileSync('src/main/java/com/sevendeadlysins/SevenDeadlySinsMod.java', 'utf8');
const modAttachmentTypesJava = fs.readFileSync('src/main/java/com/sevendeadlysins/registry/ModAttachmentTypes.java', 'utf8');
const forbiddenFruitJava = fs.readFileSync('src/main/java/com/sevendeadlysins/item/ForbiddenFruitItem.java', 'utf8');
const modItemsJava = fs.readFileSync('src/main/java/com/sevendeadlysins/registry/ModItems.java', 'utf8');
const modEntitiesJava = fs.readFileSync('src/main/java/com/sevendeadlysins/registry/ModEntities.java', 'utf8');
const addLootModifierJava = fs.readFileSync('src/main/java/com/sevendeadlysins/loot/AddLootModifier.java', 'utf8');
const modLootModifiersJava = fs.readFileSync('src/main/java/com/sevendeadlysins/registry/ModLootModifiers.java', 'utf8');

const playerSinsDataJava = fs.readFileSync('src/main/java/com/sevendeadlysins/data/PlayerSinsData.java', 'utf8');
const castSinPayloadJava = fs.readFileSync('src/main/java/com/sevendeadlysins/network/CastSinPayload.java', 'utf8');
const syncSinsDataPayloadJava = fs.readFileSync('src/main/java/com/sevendeadlysins/network/SyncSinsDataPayload.java', 'utf8');
const selectSinPayloadJava = fs.readFileSync('src/main/java/com/sevendeadlysins/network/SelectSinPayload.java', 'utf8');
const modNetworkJava = fs.readFileSync('src/main/java/com/sevendeadlysins/network/ModNetwork.java', 'utf8');

const engineJava = fs.readFileSync('src/main/java/com/sevendeadlysins/ability/SinsAbilityEngine.java', 'utf8');
const eventsJava = fs.readFileSync('src/main/java/com/sevendeadlysins/event/SinsCommonEvents.java', 'utf8');
const soulOrbJava = fs.readFileSync('src/main/java/com/sevendeadlysins/entity/SoulOrbEntity.java', 'utf8');
const keyBindingsJava = fs.readFileSync('src/main/java/com/sevendeadlysins/client/ModKeyBindings.java', 'utf8');
const soulOrbRendererJava = fs.readFileSync('src/main/java/com/sevendeadlysins/client/SoulOrbRenderer.java', 'utf8');
const hudJava = fs.readFileSync('src/main/java/com/sevendeadlysins/client/SinsHudOverlay.java', 'utf8');
const radialJava = fs.readFileSync('src/main/java/com/sevendeadlysins/client/RadialMenuScreen.java', 'utf8');
const greedJava = fs.readFileSync('src/main/java/com/sevendeadlysins/client/GreedCatalogScreen.java', 'utf8');
const envyJava = fs.readFileSync('src/main/java/com/sevendeadlysins/client/EnvySelectionScreen.java', 'utf8');
const compatJava = fs.readFileSync('src/main/java/com/sevendeadlysins/compat/CompatManager.java', 'utf8');

const section1And2Ts = `import { ModProjectFile } from './modTypes';

export const FILES_SECTION_1_2: ModProjectFile[] = [
  {
    id: 'build-gradle',
    sectionOrder: 1,
    sectionTitle: '1. Конфигурационные файлы сборки 1.20.1 (Forge 47.3.0 / Java 17) и GitHub Actions CI/CD',
    path: 'build.gradle',
    filename: 'build.gradle',
    language: 'groovy',
    description: 'Скрипт сборки ForgeGradle 6.0 под Minecraft 1.20.1 (Forge 47.3.0), Java 17 и официальные маппинги (автоматически генерирует валидный pack.mcmeta с pack_format: 15 при сборке).',
    content: ${JSON.stringify(buildGradle)}
  },
  {
    id: 'gradle-properties',
    sectionOrder: 1,
    sectionTitle: '1. Конфигурационные файлы сборки 1.20.1 (Forge 47.3.0 / Java 17) и GitHub Actions CI/CD',
    path: 'gradle.properties',
    filename: 'gradle.properties',
    language: 'properties',
    description: 'Параметры G1GC JVM, версии Minecraft 1.20.1, Forge 47.3.0 и метаданные издания Dragonfyre.',
    content: ${JSON.stringify(gradleProperties)}
  },
  {
    id: 'settings-gradle',
    sectionOrder: 1,
    sectionTitle: '1. Конфигурационные файлы сборки 1.20.1 (Forge 47.3.0 / Java 17) и GitHub Actions CI/CD',
    path: 'settings.gradle',
    filename: 'settings.gradle',
    language: 'groovy',
    description: 'Настройка репозитория MinecraftForge и автоматического резолвера JDK 17 (Foojay Toolchain).',
    content: ${JSON.stringify(settingsGradle)}
  },
  {
    id: 'pack-mcmeta',
    sectionOrder: 1,
    sectionTitle: '1. Конфигурационные файлы сборки 1.20.1 (Forge 47.3.0 / Java 17) и GitHub Actions CI/CD',
    path: 'src/main/resources/pack.mcmeta',
    filename: 'pack.mcmeta',
    language: 'json',
    description: 'КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ ОШИБКИ РЕСУРСОВ FORGE 1.20.1: Дескриптор ресурсов с pack_format: 15 и объектом description.text (устраняет ошибку «не удалось загрузить правильную информацию о наборе ресурсов»).',
    content: ${JSON.stringify(packMcmeta)}
  },
  {
    id: 'mods-toml',
    sectionOrder: 1,
    sectionTitle: '1. Конфигурационные файлы сборки 1.20.1 (Forge 47.3.0 / Java 17) и GitHub Actions CI/CD',
    path: 'src/main/resources/META-INF/mods.toml',
    filename: 'mods.toml',
    language: 'toml',
    description: 'Дескриптор мода META-INF/mods.toml для загрузчика Forge 1.20.1 с универсальными диапазонами [0,) для всех бета-версий модов Cisco\\'s RPG [Dragonfyre] 3C Beta.',
    content: ${JSON.stringify(modsToml)}
  },
  {
    id: 'github-workflow-build',
    sectionOrder: 1,
    sectionTitle: '1. Конфигурационные файлы сборки 1.20.1 (Forge 47.3.0 / Java 17) и GitHub Actions CI/CD',
    path: '.github/workflows/build.yml',
    filename: 'build.yml (GitHub Actions)',
    language: 'toml',
    description: 'Автоматический пайплайн GitHub Actions: устанавливает JDK 17 (Temurin) и Gradle 8.8, принудительно проверяет pack.mcmeta (pack_format: 15) и компилирует готовый .jar под Minecraft 1.20.1.',
    content: ${JSON.stringify(workflowYml)}
  },
  {
    id: 'main-mod-class',
    sectionOrder: 2,
    sectionTitle: '2. Главный класс, TPS-Кэш Данных Игрока, Запретный Плод и Loot Modifier (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/SevenDeadlySinsMod.java',
    filename: 'SevenDeadlySinsMod.java',
    language: 'java',
    description: 'Точка входа @Mod("sevendeadlysins") для Forge 1.20.1. Инициализирует DeferredRegister, сетевой канал SimpleChannel и интеграции с Cisco\\'s RPG [Dragonfyre].',
    content: ${JSON.stringify(mainModJava)}
  },
  {
    id: 'mod-attachment-types',
    sectionOrder: 2,
    sectionTitle: '2. Главный класс, TPS-Кэш Данных Игрока, Запретный Плод и Loot Modifier (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/registry/ModAttachmentTypes.java',
    filename: 'ModAttachmentTypes.java',
    language: 'java',
    description: 'TPS-оптимизированный менеджер данных игрока для 1.20.1: O(1) кэш в оперативной памяти + сохранение в Player.PERSISTED_NBT_TAG (переживает смерть и смену измерений без падения TPS).',
    content: ${JSON.stringify(modAttachmentTypesJava)}
  },
  {
    id: 'forbidden-fruit-item',
    sectionOrder: 2,
    sectionTitle: '2. Главный класс, TPS-Кэш Данных Игрока, Запретный Плод и Loot Modifier (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/item/ForbiddenFruitItem.java',
    filename: 'ForbiddenFruitItem.java',
    language: 'java',
    description: 'Предмет ForbiddenFruitItem (1.20.1 API): огнестойкий эпический реликт, пробуждает Семь Грехов, повышает Ранг Души Dragonfyre (+1), даёт +50 маны и сбрасывает КД Iron\\'s Spells.',
    content: ${JSON.stringify(forbiddenFruitJava)}
  },
  {
    id: 'mod-items',
    sectionOrder: 2,
    sectionTitle: '2. Главный класс, TPS-Кэш Данных Игрока, Запретный Плод и Loot Modifier (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/registry/ModItems.java',
    filename: 'ModItems.java',
    language: 'java',
    description: 'Регистрация предмета ForbiddenFruitItem через ForgeRegistries.ITEMS (1.20.1).',
    content: ${JSON.stringify(modItemsJava)}
  },
  {
    id: 'mod-entities',
    sectionOrder: 2,
    sectionTitle: '2. Главный класс, TPS-Кэш Данных Игрока, Запретный Плод и Loot Modifier (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/registry/ModEntities.java',
    filename: 'ModEntities.java',
    language: 'java',
    description: 'Регистрация сущности сферы души SoulOrbEntity через ForgeRegistries.ENTITY_TYPES (1.20.1).',
    content: ${JSON.stringify(modEntitiesJava)}
  },
  {
    id: 'loot-modifier',
    sectionOrder: 2,
    sectionTitle: '2. Главный класс, TPS-Кэш Данных Игрока, Запретный Плод и Loot Modifier (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/loot/AddLootModifier.java',
    filename: 'AddLootModifier.java',
    language: 'java',
    description: 'Глобальный модификатор лута 1.20.1: добавляет Запретный Плод в Древний Город (15%), Бастионы, Города Энда, а также сундуки боссов Cataclysm и логовищ драконов Ice & Fire (12%).',
    content: ${JSON.stringify(addLootModifierJava)}
  },
  {
    id: 'mod-loot-modifiers',
    sectionOrder: 2,
    sectionTitle: '2. Главный класс, TPS-Кэш Данных Игрока, Запретный Плод и Loot Modifier (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/registry/ModLootModifiers.java',
    filename: 'ModLootModifiers.java',
    language: 'java',
    description: 'Регистрация Codec<AddLootModifier> в ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS (1.20.1).',
    content: ${JSON.stringify(modLootModifiersJava)}
  }
];
`;

const section3Ts = `import { ModProjectFile } from './modTypes';

export const FILES_SECTION_3: ModProjectFile[] = [
  {
    id: 'player-sins-data',
    sectionOrder: 3,
    sectionTitle: '3. Данные игрока (PlayerSinsData) и Сетевой Канал SimpleChannel (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/data/PlayerSinsData.java',
    filename: 'PlayerSinsData.java',
    language: 'java',
    description: 'Класс данных игрока (INBTSerializable<CompoundTag>, Java 17): хранит ману, Ранг Души Dragonfyre, каталог артефактов Алчности (с чарами Apotheosis), коллекцию способностей Зависти и флаг dirty для экономии трафика.',
    content: ${JSON.stringify(playerSinsDataJava)}
  },
  {
    id: 'cast-sin-payload',
    sectionOrder: 3,
    sectionTitle: '3. Данные игрока (PlayerSinsData) и Сетевой Канал SimpleChannel (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/network/CastSinPayload.java',
    filename: 'CastSinPayload.java',
    language: 'java',
    description: 'Пакет C2S (FriendlyByteBuf): применение активного навыка Греха, репликация предмета Алчности или выбор украденной способности Dragonfyre.',
    content: ${JSON.stringify(castSinPayloadJava)}
  },
  {
    id: 'sync-sins-data-payload',
    sectionOrder: 3,
    sectionTitle: '3. Данные игрока (PlayerSinsData) и Сетевой Канал SimpleChannel (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/network/SyncSinsDataPayload.java',
    filename: 'SyncSinsDataPayload.java',
    language: 'java',
    description: 'Пакет S2C (FriendlyByteBuf): быстрая синхронизация CompoundTag состояния игрока с сервера на клиент.',
    content: ${JSON.stringify(syncSinsDataPayloadJava)}
  },
  {
    id: 'select-sin-payload',
    sectionOrder: 3,
    sectionTitle: '3. Данные игрока (PlayerSinsData) и Сетевой Канал SimpleChannel (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/network/SelectSinPayload.java',
    filename: 'SelectSinPayload.java',
    language: 'java',
    description: 'Пакет C2S (FriendlyByteBuf): выбор активного Греха из радиального меню (клавиша R или 1-7).',
    content: ${JSON.stringify(selectSinPayloadJava)}
  },
  {
    id: 'mod-network',
    sectionOrder: 3,
    sectionTitle: '3. Данные игрока (PlayerSinsData) и Сетевой Канал SimpleChannel (1.20.1)',
    path: 'src/main/java/com/sevendeadlysins/network/ModNetwork.java',
    filename: 'ModNetwork.java',
    language: 'java',
    description: 'Регистрация пакетов в NetworkRegistry.newSimpleChannel (Forge 1.20.1) и обработчики пакетов.',
    content: ${JSON.stringify(modNetworkJava)}
  }
];
`;

const section4aTs = `import { ModProjectFile } from './modTypes';

export const FILES_SECTION_4A: ModProjectFile[] = [
  {
    id: 'sins-ability-engine',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/ability/SinsAbilityEngine.java',
    filename: 'SinsAbilityEngine.java',
    language: 'java',
    description: 'Серверный движок 7 Грехов под 1.20.1: гибридный % урон от макс. HP для боссов Dragonfyre, копирование мифических предметов Apotheosis и кража дыхания драконов Ice & Fire.',
    content: ${JSON.stringify(engineJava)}
  },
  {
    id: 'sins-common-events',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/event/SinsCommonEvents.java',
    filename: 'SinsCommonEvents.java',
    language: 'java',
    description: 'TPS-оптимизированные обработчики событий Forge 1.20.1: 0.00ms нагрузки на фоновых мобов (убран глобальный LivingTickEvent), синхронизация только при dirty-флаге.',
    content: ${JSON.stringify(eventsJava)}
  },
  {
    id: 'soul-orb-entity',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/entity/SoulOrbEntity.java',
    filename: 'SoulOrbEntity.java',
    language: 'java',
    description: 'Сущность сферы души Чревоугодия (1.20.1 NetworkHooks): поглощение душ боссов даёт +50 макс. маны, +1 Ранг Души Dragonfyre и сбрасывает КД Iron\\'s Spells.',
    content: ${JSON.stringify(soulOrbJava)}
  }
];
`;

const section4bTs = `import { ModProjectFile } from './modTypes';

export const FILES_SECTION_4B: ModProjectFile[] = [
  {
    id: 'mod-key-bindings',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/client/ModKeyBindings.java',
    filename: 'ModKeyBindings.java',
    language: 'java',
    description: 'Клавиши управления (R, V, X) с KeyConflictContext.IN_GAME (не конфликтуют с JEI и окнами RPG), регистрация SoulOrbRenderer и RegisterGuiOverlaysEvent.',
    content: ${JSON.stringify(keyBindingsJava)}
  },
  {
    id: 'soul-orb-renderer',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/client/SoulOrbRenderer.java',
    filename: 'SoulOrbRenderer.java',
    language: 'java',
    description: 'Клиентский рендерер сферы души Чревоугодия для 1.20.1 (Matrix4f / VertexConsumer.vertex), полностью совместимый с Embeddium и шейдерами Oculus.',
    content: ${JSON.stringify(soulOrbRendererJava)}
  },
  {
    id: 'sins-hud-overlay',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/client/SinsHudOverlay.java',
    filename: 'SinsHudOverlay.java',
    language: 'java',
    description: 'Клиентский HUD оверлей (IGuiOverlay 1.20.1): отображает активный грех, Ранг Души Dragonfyre, полосу маны, процент Гнева и выбранный навык Зависти.',
    content: ${JSON.stringify(hudJava)}
  },
  {
    id: 'radial-menu-screen',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/client/RadialMenuScreen.java',
    filename: 'RadialMenuScreen.java',
    language: 'java',
    description: 'Радиальное меню выбора активного Греха для 1.20.1 с описанием бонусов под Cisco\\'s RPG [Dragonfyre].',
    content: ${JSON.stringify(radialJava)}
  },
  {
    id: 'greed-catalog-screen',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/client/GreedCatalogScreen.java',
    filename: 'GreedCatalogScreen.java',
    language: 'java',
    description: 'Интерфейс Сокровищницы Алчности (1.20.1 ItemStack.of): отображает и реплицирует скопированные блоки и артефакты со всеми чарами и аффиксами Apotheosis.',
    content: ${JSON.stringify(greedJava)}
  },
  {
    id: 'envy-selection-screen',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/client/EnvySelectionScreen.java',
    filename: 'EnvySelectionScreen.java',
    language: 'java',
    description: 'Экран Арсенала Зависти: позволяет выбирать похищенные способности Драконов Ice & Fire, Боссов Cataclysm, заклинаний Iron\\'s Spells и оружия Simply Swords.',
    content: ${JSON.stringify(envyJava)}
  },
  {
    id: 'compat-manager',
    sectionOrder: 4,
    sectionTitle: '4. Движок 7 Грехов, Оптимизация TPS и Интеграция с Cisco\\'s RPG [Dragonfyre]',
    path: 'src/main/java/com/sevendeadlysins/compat/CompatManager.java',
    filename: 'CompatManager.java',
    language: 'java',
    description: 'Модуль глубокой интеграции с модпаком Cisco\\'s Fantasy Medieval RPG [Dragonfyre]: связывает Грехи с атрибутами Apotheosis (Crit/Armor Shred/Life Steal), Iron\\'s Spells, драконами Ice & Fire, боссами Cataclysm и L2Hostility.',
    content: ${JSON.stringify(compatJava)}
  }
];
`;

fs.writeFileSync('src/data/filesSection1And2.ts', section1And2Ts, 'utf8');
fs.writeFileSync('src/data/filesSection3.ts', section3Ts, 'utf8');
fs.writeFileSync('src/data/filesSection4a.ts', section4aTs, 'utf8');
fs.writeFileSync('src/data/filesSection4b.ts', section4bTs, 'utf8');
console.log('Synced all 4 TS sections including pack.mcmeta successfully.');
