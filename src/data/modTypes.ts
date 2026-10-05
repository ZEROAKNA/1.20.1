export interface ModProjectFile {
  id: string;
  sectionOrder: 1 | 2 | 3 | 4;
  sectionTitle: string;
  path: string;
  filename: string;
  language: 'groovy' | 'properties' | 'toml' | 'java' | 'json';
  description: string;
  content: string;
}

export interface SinSpecification {
  id: number;
  codeName: string;
  titleRu: string;
  titleEn: string;
  cooldownTicks: number;
  manaCost: number;
  modes: string[];
  summary: string;
  activeSummary: string;
  passiveSummary: string;
}

export const SINS_SPECS: SinSpecification[] = [
  {
    id: 0,
    codeName: 'PRIDE',
    titleRu: 'Гордыня',
    titleEn: 'Pride',
    cooldownTicks: 200,
    manaCost: 35,
    modes: [
      '0: Остановка Времени (56 бл. + Заморозка снарядов в воздухе)',
      '1: Гравитационный Коллапс (45 бл. + 85 + 12% Max HP Босса + Молнии)',
      '2: Солнечный Зенит (Полдень + Полная Неуязвимость + Огненная Аура)'
    ],
    summary: 'Закон 0 замораживает врагов и летящие снаряды в радиусе 56 блоков (повтор V — снять). Закон 1 наносит гибридный урон 85 + 12% от макс. HP боссов Cataclysm / Драконов Ice & Fire.',
    activeSummary: '[V] Остановка времени (56 бл.), Гравитационный коллапс (85 + 12% Max HP) или Солнечный зенит (Неуязвимость).',
    passiveSummary: 'Пассивно: Локальный тик-обработчик (0.00ms нагрузки на TPS) удерживает снаряды и боссов в заморозке.'
  },
  {
    id: 1,
    codeName: 'GREED',
    titleRu: 'Алчность',
    titleEn: 'Greed',
    cooldownTicks: 20,
    manaCost: 10,
    modes: [
      '0: Копия БЛОКА в инвентарь + Скан экипировки Apotheosis (24 бл.)',
      '1: Сокровищница Алчности (Репликация со всеми чарами и сокетами)'
    ],
    summary: 'Копирует любой блок по взгляду прямо в инвентарь, притягивает лут, даёт Везение III (повышает шанс мифических предметов Apotheosis) и реплицирует экипировку через ItemStack.of(tag) со всеми аффиксами, сокетами и самоцветами.',
    activeSummary: '[V] Копирует блок по взгляду в инвентарь (10 маны) или открывает Сокровищницу Артефактов (30 маны).',
    passiveSummary: 'Пассивно: Сохраняет 100% мифических аффиксов Apotheosis, самоцветов, чар и свитков Iron\'s Spells.'
  },
  {
    id: 2,
    codeName: 'LUST',
    titleRu: 'Похоть',
    titleEn: 'Lust',
    cooldownTicks: 100,
    manaCost: 25,
    modes: ['0: Приручение цели и стаи (8 бл.) как верной собаки (+85% урона)'],
    summary: 'Приручает цель и до 5 мобов рядом как верных собак: они получают имя питомца, бегают за вами, телепортируются при отставании (>16 бл.), лечатся и атакуют всех врагов хозяина.',
    activeSummary: '[V] Приручает выбранную цель и до 5 существ рядом как верных телохранителей (+85% к атаке).',
    passiveSummary: 'Пассивно: Обновление ИИ питомцев оптимизировано (раз в 10 тиков), исключён дружественный огонь.'
  },
  {
    id: 3,
    codeName: 'ENVY',
    titleRu: 'Зависть',
    titleEn: 'Envy',
    cooldownTicks: 60,
    manaCost: 20,
    modes: [
      '0: Кража способности (Ice & Fire, Cataclysm, Iron\'s Spells, L2Hostility)',
      '1: Применение выбранной способности (% от Макс. HP цели)',
      '2: Меню Арсенала Зависти (EnvySelectionScreen)'
    ],
    summary: 'Крадёт Огненное/Ледяное/Грозовое дыхание Драконов Ice & Fire, навыки боссов Cataclysm (Игнис, Маледиктус, Предвестник, Левиафан), магию Iron\'s Spells и оружие Simply Swords, одновременно срывая баффы с элитных мобов L2Hostility.',
    activeSummary: '[V] Крадёт навыки Драконов и Боссов Dragonfyre в личную коллекцию или применяет выбранный навык.',
    passiveSummary: 'Пассивно: При краже снимает все положительные баффы и регенерацию с адаптивных мобов L2Hostility.'
  },
  {
    id: 4,
    codeName: 'GLUTTONY',
    titleRu: 'Чревоугодие',
    titleEn: 'Gluttony',
    cooldownTicks: 60,
    manaCost: 20,
    modes: ['0: Воронка Бездны + Поглощение сфер душ (SoulOrbEntity)'],
    summary: 'При убийстве мобов спавнится SoulOrbEntity (+3 макс. маны; +50 макс. маны и +1 Ранг Души Dragonfyre за босса + мгновенный сброс всех кулдаунов Iron\'s Spells). Активный каст стягивает врагов в воронку и пьёт их HP.',
    activeSummary: '[V] Стягивает врагов в радиусе 14 блоков, нанося гибридный % урон от HP и восстанавливая ману и HP.',
    passiveSummary: 'Пассивно: Души боссов дают +50 Max Mana (включая ману Iron\'s Spells), +1 Ранг Dragonfyre и сброс КД.'
  },
  {
    id: 5,
    codeName: 'WRATH',
    titleRu: 'Гнев',
    titleEn: 'Wrath',
    cooldownTicks: 200,
    manaCost: 0,
    modes: ['0: Катаклизм Берсерка (+35% стаков + Взрывная волна % Max HP)'],
    summary: 'Стаки Гнева (0–100%) динамически повышают не только физ. урон (+35% за тир), но и атрибуты Apotheosis (Crit Chance, Crit Damage, Armor Shred, Life Steal) и Spell Power (Iron\'s Spells). На 100% — режим Берсерка!',
    activeSummary: '[V] Мгновенно разгоняет +35% стаков Гнева и обрушивает огненно-взрывной катаклизм в радиусе 12 блоков.',
    passiveSummary: 'Пассивно: Масштабирует Crit Chance, Crit Damage, Armor Shred (Apotheosis) и Spell Power (Iron\'s Spells).'
  },
  {
    id: 6,
    codeName: 'SLOTH',
    titleRu: 'Лень',
    titleEn: 'Sloth',
    cooldownTicks: 200,
    manaCost: 0,
    modes: [
      '0: Гибернация Короля (100% HP + Снятие проклятий + 20 Золотых сердец -> 5x Урон)',
      '1: Покой Лени (Пассивно +22 маны/сек и +5 HP/сек стоя на месте)'
    ],
    summary: 'Мгновенно исцеляет 100% HP и маны, снимает все дебаффы и проклятия L2Hostility, усыпляет врагов вокруг и через 10 сек включает 5-кратное Пробуждение Короля (x5 урон и +100% Spell Power на 3 минуты).',
    activeSummary: '[V] Полное исцеление, снятие всех дебаффов, 20 золотых сердец и активация 5x Пробуждения Короля.',
    passiveSummary: 'Пассивно: 5 секунд без движения включают Покой (+22 маны/сек и +5 HP/сек).'
  }
];
