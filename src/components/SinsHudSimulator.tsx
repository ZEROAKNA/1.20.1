import React, { useState, useEffect, useCallback } from 'react';
import { SINS_SPECS } from '../data/modTypes';
import { RotateCcw, Play, Pause, ShieldAlert, Sparkles, Flame } from 'lucide-react';

interface SimulatedArtifact {
  id: string;
  name: string;
  enchantments: string;
  sourceMod: 'minecraft' | 'irons_spellbooks' | 'apotheosis' | 'simplyswords';
  manaCost: number;
}

const SAMPLE_SCAN_TARGETS: SimulatedArtifact[] = [
  {
    id: 'apotheosis_mythic_claymore',
    name: 'Mythic Brimstone Claymore [Apotheosis + Simply Swords]',
    enchantments: 'Affix: Ancient Dragonfyre (+35% Crit Dmg, +18% Armor Shred, 3x Perfect Royal Gems)',
    sourceMod: 'apotheosis',
    manaCost: 30,
  },
  {
    id: 'irons_blood_staff',
    name: "Blood Staff [Iron's Spells 1.20.1]",
    enchantments: 'NBT ISpellContainer: [Blood Slash Lv.6, Heartstop Lv.4, Ray of Siphoning Lv.5]',
    sourceMod: 'irons_spellbooks',
    manaCost: 30,
  },
  {
    id: 'dragonsteel_chestplate',
    name: 'Dragonsteel Fire Chestplate [Ice and Fire]',
    enchantments: 'Защита VIII, Огнестойкость Дракона, Шипы IV, Починка',
    sourceMod: 'apotheosis',
    manaCost: 30,
  },
  {
    id: 'diamond_block_copy',
    name: 'Алмазный блок (Скопирован по взгляду)',
    enchantments: 'Блок мира (Raytrace Block -> Скопирован в инвентарь за 10 маны)',
    sourceMod: 'minecraft',
    manaCost: 10,
  },
];

export const SinsHudSimulator: React.FC = () => {
  // PlayerSinsData state (1.20.1 Dragonfyre Edition)
  const [unlocked, setUnlocked] = useState<boolean>(true);
  const [currentMana, setCurrentMana] = useState<number>(150);
  const [maxMana, setMaxMana] = useState<number>(150);
  const [dragonfyreSoulRank, setDragonfyreSoulRank] = useState<number>(2);
  const [activeSinIndex, setActiveSinIndex] = useState<number>(0);
  const [activeSubMode, setActiveSubMode] = useState<number>(0);

  const [gameTimeTicks, setGameTimeTicks] = useState<number>(6000);
  const [isTickRunning, setIsTickRunning] = useState<boolean>(true);

  const [prideCooldownUntil, setPrideCooldownUntil] = useState<number>(0);
  const [prideActiveUntil, setPrideActiveUntil] = useState<number>(0);
  const [activePrideLaw, setActivePrideLaw] = useState<number>(-1);

  const [wrathStacks, setWrathStacks] = useState<number>(30);
  const [wrathBerserkUntil, setWrathBerserkUntil] = useState<number>(0);

  const [slothLockedUntil, setSlothLockedUntil] = useState<number>(0);
  const [slothBuffUntil, setSlothBuffUntil] = useState<number>(0);
  const [slothResting, setSlothResting] = useState<boolean>(false);

  const [observedArtifacts, setObservedArtifacts] = useState<SimulatedArtifact[]>([
    SAMPLE_SCAN_TARGETS[0],
    SAMPLE_SCAN_TARGETS[1],
  ]);
  const [replicatedInventory, setReplicatedInventory] = useState<string[]>([]);
  const [stolenAbilityId, setStolenAbilityId] = useState<string>('dragonfyre:fire_dragon_breath');
  const [lustTargetName, setLustTargetName] = useState<string>('Ignis / L2Hostility Champion [Gaze > 1.5s]');
  const [nbtDirtySynced, setNbtDirtySynced] = useState<boolean>(false);

  const [radialMenuOpen, setRadialMenuOpen] = useState<boolean>(false);
  const [greedCatalogOpen, setGreedCatalogOpen] = useState<boolean>(false);
  const [actionBarMessage, setActionBarMessage] = useState<string>(
    'Симулятор 1.20.1 [Cisco\'s RPG Dragonfyre]: нажмите [R] для колеса Грехов, [X] для подрежима, [V] для каста.'
  );

  const currentSin = SINS_SPECS[activeSinIndex];

  // Passive tick loop (1 second = 20 ticks)
  useEffect(() => {
    if (!isTickRunning || !unlocked) return;
    const interval = setInterval(() => {
      setGameTimeTicks((prev) => prev + 20);
      setCurrentMana((prev) => {
        let regen = slothResting ? 22 : 3.5 + dragonfyreSoulRank * 0.5;
        if (gameTimeTicks >= slothLockedUntil && gameTimeTicks < slothBuffUntil) {
          regen *= 5;
        }
        const next = Math.min(maxMana, prev + regen);
        setNbtDirtySynced(Math.abs(next - prev) > 0.01);
        return next;
      });
    }, 1000);
    return () => clearInterval(interval);
  }, [isTickRunning, unlocked, slothResting, maxMana, gameTimeTicks, slothLockedUntil, slothBuffUntil, dragonfyreSoulRank]);

  const cycleSubMode = useCallback(() => {
    if (!unlocked) return;
    const maxModes = currentSin.modes.length;
    const next = (activeSubMode + 1) % maxModes;
    setActiveSubMode(next);
    setNbtDirtySynced(true);
    setActionBarMessage(`Подрежим ${currentSin.titleRu}: ${currentSin.modes[next]}`);
  }, [unlocked, currentSin, activeSubMode]);

  const castActiveSin = useCallback(() => {
    if (!unlocked) {
      setActionBarMessage('Грехи скованы! Сначала съешьте Запретный Плод (64 тика).');
      return;
    }

    if (gameTimeTicks < slothLockedUntil) {
      const rem = Math.ceil((slothLockedUntil - gameTimeTicks) / 20);
      setActionBarMessage(`Печать Лени сковывает активные навыки ещё ${rem} сек.`);
      return;
    }

    switch (activeSinIndex) {
      case 0: {
        // Pride
        if (activeSubMode === 0 && activePrideLaw === 0 && gameTimeTicks < prideActiveUntil) {
          setActivePrideLaw(-1);
          setPrideActiveUntil(0);
          setActionBarMessage('Гордыня: Остановка Времени досрочно снята повторным нажатием [V]!');
          return;
        }
        if (gameTimeTicks < prideCooldownUntil) {
          setActionBarMessage(
            `Гордыня восстанавливается: осталось ${Math.ceil((prideCooldownUntil - gameTimeTicks) / 20)} сек.`
          );
          return;
        }
        if (currentMana < 35) {
          setActionBarMessage('Недостаточно маны для Закона Гордыни (требуется 35 ед.).');
          return;
        }
        setCurrentMana((m) => m - 35);
        setActivePrideLaw(activeSubMode);
        setPrideActiveUntil(gameTimeTicks + 900); // 45 sec
        setPrideCooldownUntil(gameTimeTicks + 200); // 10 sec
        const lawNames = [
          'Остановка Времени (56 бл., мобы и снаряды заморожены на 45 сек; повтор V — снять)',
          'Гравитационный Коллапс (45 бл., подброс и обрушение на 85 + 12% Max HP Босса + Молнии)',
          'Солнечный Зенит (6000L полдень, Полная Неуязвимость, Сила IV и Солнечная Аура)',
        ];
        setActionBarMessage(`Закон Гордыни активирован: ${lawNames[activeSubMode]}`);
        break;
      }
      case 1: {
        // Greed
        if (activeSubMode === 1) {
          setGreedCatalogOpen(true);
          setActionBarMessage('Открыт Каталог Алчности (GreedCatalogScreen 1.20.1). Выберите артефакт с аффиксами Apotheosis.');
        } else {
          const nextSample = SAMPLE_SCAN_TARGETS[observedArtifacts.length % SAMPLE_SCAN_TARGETS.length];
          if (observedArtifacts.length < 20) {
            setObservedArtifacts((prev) => [...prev, { ...nextSample, id: `${nextSample.id}_${Date.now()}` }]);
          }
          setReplicatedInventory((inv) => [nextSample.name, ...inv.slice(0, 4)]);
          setActionBarMessage(
            `Алчность (24 бл.): Скопирован "${nextSample.name}" со всеми NBT-тегами Apotheosis (${Math.min(20, observedArtifacts.length + 1)}/20)!`
          );
        }
        break;
      }
      case 2: {
        // Lust
        if (currentMana < 25) {
          setActionBarMessage('Недостаточно маны для Похоти (требуется 25 ед.).');
          return;
        }
        setCurrentMana((m) => m - 25);
        setActionBarMessage(
          `Похоть: Приручена цель [${lustTargetName}] и до 5 мобов рядом как верные собаки (+85% урона, ТП за хозяином)!`
        );
        break;
      }
      case 3: {
        // Envy
        const abilities = [
          'dragonfyre:fire_dragon_breath',
          'dragonfyre:ice_dragon_breath',
          'dragonfyre:lightning_dragon_breath',
          'cataclysm:ignis_abyssal_burn',
          'cataclysm:maledictus_phantom_halberd',
          'cataclysm:harbinger_death_laser',
          'irons_spell:eldritch_blast',
          'mod_weapon:simplyswords:soulrender',
          'warden_sonic_boom',
          'enderman_blink',
        ];
        if (activeSubMode === 0) {
          if (currentMana < 20) {
            setActionBarMessage('Недостаточно маны для кражи способности (требуется 20 ед.).');
            return;
          }
          setCurrentMana((m) => m - 20);
          const nextIdx = (abilities.indexOf(stolenAbilityId) + 1) % abilities.length;
          const nextAbil = abilities[nextIdx];
          setStolenAbilityId(nextAbil);
          setActionBarMessage(
            `Зависть [Dragonfyre]: Сорваны баффы L2Hostility и похищена способность [${nextAbil}] в Арсенал Зависти!`
          );
        } else if (activeSubMode === 2) {
          const nextIdx = (abilities.indexOf(stolenAbilityId) + 1) % abilities.length;
          const nextAbil = abilities[nextIdx];
          setStolenAbilityId(nextAbil);
          setActionBarMessage(
            `Арсенал Зависти (EnvySelectionScreen): Выбрана способность [${nextAbil}]! Нажмите [V] для каста.`
          );
        } else {
          setActionBarMessage(
            `Зависть [Dragonfyre]: Применена способность [${stolenAbilityId}] (Гибридный урон + % от Макс. HP цели)!`
          );
        }
        break;
      }
      case 4: {
        // Gluttony
        const gain = 6;
        setMaxMana((m) => m + gain);
        setCurrentMana((m) => Math.min(maxMana + gain, m + 45));
        setActionBarMessage(
          `Чревоугодие: Воронка Бездны поглотила души врагов! Макс. мана +${gain} (теперь ${maxMana + gain}), восстановлены HP и Мана.`
        );
        break;
      }
      case 5: {
        // Wrath
        const nextStacks = Math.min(100, wrathStacks + 35);
        setWrathStacks(nextStacks);
        if (nextStacks >= 100) {
          setWrathBerserkUntil(gameTimeTicks + 400);
        }
        const mult =
          (1 + dragonfyreSoulRank * 0.08) *
          (nextStacks >= 100 || gameTimeTicks < wrathBerserkUntil ? 3 : 1 + Math.floor(nextStacks / 10) * 0.35);
        setActionBarMessage(
          `КАТАКЛИЗМ ГНЕВА (${nextStacks}%): Взрывная волна % Max HP! Множитель урона: x${mult.toFixed(2)} + бонус Crit/Armor Shred Apotheosis.`
        );
        break;
      }
      case 6: {
        // Sloth
        const lockUntil = gameTimeTicks + 200; // 10 seconds sleep -> 3 minutes 5x Awakening
        setSlothLockedUntil(lockUntil);
        setSlothBuffUntil(lockUntil + 3600);
        setCurrentMana(maxMana);
        setActionBarMessage(
          'Гибернация Короля Лени: 100% HP и Маны, сняты проклятия L2Hostility, выдано 20 Золотых сердец! Через 10 сек — 5x Пробуждение!'
        );
        break;
      }
    }
    setNbtDirtySynced(true);
  }, [
    unlocked,
    gameTimeTicks,
    slothLockedUntil,
    activeSinIndex,
    activePrideLaw,
    prideActiveUntil,
    prideCooldownUntil,
    currentMana,
    activeSubMode,
    observedArtifacts,
    lustTargetName,
    stolenAbilityId,
    maxMana,
    wrathBerserkUntil,
    wrathStacks,
    dragonfyreSoulRank,
  ]);

  // Keyboard listener for R, V, X
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if (['INPUT', 'TEXTAREA'].includes((e.target as HTMLElement)?.tagName)) return;
      const key = e.key.toUpperCase();
      if (key === 'R') {
        e.preventDefault();
        setRadialMenuOpen((prev) => !prev);
        setGreedCatalogOpen(false);
      } else if (key === 'X') {
        e.preventDefault();
        cycleSubMode();
      } else if (key === 'V') {
        e.preventDefault();
        castActiveSin();
      }
    };
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [cycleSubMode, castActiveSin]);

  const wrathTiers = Math.floor(wrathStacks / 10);
  const isBerserk = gameTimeTicks < wrathBerserkUntil;
  const isSlothAwakened = gameTimeTicks >= slothLockedUntil && gameTimeTicks < slothBuffUntil && slothBuffUntil > 0;
  const isSlothLocked = gameTimeTicks < slothLockedUntil;

  // Apotheosis & Iron's Spells computed bonuses
  const ironsSpellPowerBonus = Math.round(
    (wrathTiers * 0.12 + (isBerserk ? 0.75 : 0) + (isSlothAwakened ? 1.0 : 0) + dragonfyreSoulRank * 0.03) * 100
  );
  const apotheosisCritChance = Math.round((wrathTiers * 0.04 + (isBerserk ? 0.25 : 0)) * 100);
  const apotheosisArmorShred = Math.round((wrathTiers * 0.05 + (isBerserk ? 0.35 : 0)) * 100);

  // Generate live NBT preview matching PlayerSinsData.writeRawNbt() in 1.20.1
  const nbtDump = `{
  "unlocked": ${unlocked ? '1b' : '0b'},
  "mana": ${currentMana.toFixed(1)}f,
  "maxMana": ${maxMana.toFixed(1)}f,
  "dragonfyreSoulRank": ${dragonfyreSoulRank},
  "activeSinIndex": ${activeSinIndex},
  "activeSubMode": ${activeSubMode},
  "prideCooldownUntil": ${prideCooldownUntil}L,
  "prideActiveUntil": ${prideActiveUntil}L,
  "wrathStacks": ${wrathStacks.toFixed(1)}f,
  "wrathBerserkUntil": ${wrathBerserkUntil}L,
  "slothLockedUntil": ${slothLockedUntil}L,
  "slothBuffUntil": ${slothBuffUntil}L,
  "stolenAbilityId": "${stolenAbilityId}",
  "observedArtifacts": [${observedArtifacts.length} ItemStack.of Tags],
  "apotheosis:armor_shred": "+${apotheosisArmorShred}%",
  "irons_spellbooks:spell_power": "+${ironsSpellPowerBonus}%"
}`;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
      {/* Left 7 Columns: Interactive Minecraft 1.20.1 [Dragonfyre] HUD Viewport & Controls */}
      <div className="lg:col-span-7 flex flex-col gap-5">
        <div className="border border-slate-800 bg-[#0F172A] rounded-xl p-5 flex flex-col justify-between min-h-[430px] relative overflow-hidden">
          {/* Top Viewport Bar */}
          <div className="flex flex-wrap items-center justify-between gap-3 pb-4 border-b border-slate-800/80">
            <div className="flex items-center gap-3 text-xs text-slate-400">
              <span className="font-semibold text-slate-200">
                HUD Симулятор 1.20.1 · Cisco&apos;s RPG [Dragonfyre]
              </span>
              <span aria-hidden="true">·</span>
              <span className="font-mono tabular-nums text-amber-400">Tick: {gameTimeTicks}L</span>
              <span aria-hidden="true">·</span>
              <span className="font-mono text-emerald-400">TPS Load: 0.00ms</span>
            </div>

            <div className="flex items-center gap-2">
              <button
                onClick={() => setIsTickRunning((r) => !r)}
                className="px-2.5 py-1 text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 rounded border border-slate-700 flex items-center gap-1.5 transition-colors whitespace-nowrap"
              >
                {isTickRunning ? <Pause className="w-3.5 h-3.5" /> : <Play className="w-3.5 h-3.5" />}
                {isTickRunning ? 'Пауза тиков' : 'Запустить тики'}
              </button>
              <button
                onClick={() => {
                  setUnlocked(true);
                  setCurrentMana(maxMana);
                  setPrideCooldownUntil(0);
                  setSlothLockedUntil(0);
                  setSlothBuffUntil(0);
                  setActionBarMessage('Все кулдауны сброшены (включая заклинания Iron\'s Spells), мана 100%.');
                }}
                className="px-2.5 py-1 text-xs font-medium bg-slate-800 hover:bg-slate-700 text-slate-300 rounded border border-slate-700 flex items-center gap-1.5 transition-colors whitespace-nowrap"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                Сброс КД
              </button>
            </div>
          </div>

          {/* Center Viewport: Radial Menu (R) or Greed Catalog (V) or Active Dragonfyre Status */}
          <div className="my-auto py-6 flex flex-col items-center justify-center relative">
            {radialMenuOpen ? (
              <div className="w-full flex flex-col items-center gap-4">
                <div className="text-center">
                  <div className="text-xs font-bold text-amber-400 tracking-wide">
                    ✦ КОЛЕСО СЕМИ СМЕРТНЫХ ГРЕХОВ [DRAGONFYRE 1.20.1] ✦
                  </div>
                  <div className="text-[11px] text-slate-400">
                    Нажмите на узел или клавиши [1 - 7] для выбора • ПКМ / [X] — сменить подрежим
                  </div>
                </div>

                <div className="w-72 h-72 rounded-full border-2 border-amber-500/40 bg-[#0B0F17]/95 relative flex items-center justify-center shadow-inner">
                  <div className="text-center px-4 py-2 rounded-lg border border-amber-500/40 bg-slate-950/90">
                    <div className="text-xs font-bold text-amber-400">{currentSin.titleRu.toUpperCase()}</div>
                    <div className="text-[10px] text-slate-300 font-mono mt-0.5">{currentSin.manaCost} Маны</div>
                    <div className="text-[10px] text-emerald-400 mt-0.5">● Ранг Души: {dragonfyreSoulRank}</div>
                  </div>
                  {SINS_SPECS.map((sin, i) => {
                    const angle = (2 * Math.PI * i) / 7 - Math.PI / 2;
                    const r = 108;
                    const x = Math.cos(angle) * r;
                    const y = Math.sin(angle) * r;
                    const selected = i === activeSinIndex;
                    return (
                      <button
                        key={sin.id}
                        onClick={() => {
                          setActiveSinIndex(i);
                          setActiveSubMode(0);
                          setRadialMenuOpen(false);
                          setActionBarMessage(
                            `Выбран грех: [${i + 1}] ${sin.titleRu} (${sin.titleEn}) — отправлен SelectSinPayload(${i})`
                          );
                        }}
                        style={{ transform: `translate(${x}px, ${y}px)` }}
                        className={`absolute px-2.5 py-1.5 rounded text-xs font-medium transition-colors whitespace-nowrap border ${
                          selected
                            ? 'bg-amber-500/20 text-amber-200 border-amber-400 font-semibold'
                            : 'bg-slate-900/95 text-slate-300 border-slate-700 hover:border-amber-400/70 hover:text-white'
                        }`}
                      >
                        [{i + 1}] {sin.titleRu}
                      </button>
                    );
                  })}
                </div>

                {/* Bottom sin detail card inside Radial Menu */}
                <div className="w-full max-w-xl p-3 rounded-lg bg-[#0B0F17] border border-amber-500/40 text-center space-y-1">
                  <div className="text-xs font-bold text-amber-400">
                    {currentSin.titleRu} ({currentSin.titleEn}) — Режим #{activeSubMode + 1}:{' '}
                    {currentSin.modes[activeSubMode]}
                  </div>
                  <div className="text-[11px] text-slate-200">{currentSin.activeSummary}</div>
                  <div className="text-[11px] text-slate-400">{currentSin.passiveSummary}</div>
                </div>
              </div>
            ) : greedCatalogOpen ? (
              <div className="w-full max-w-md bg-[#0B0F17] border-2 border-amber-500/60 rounded-lg p-4">
                <div className="flex items-center justify-between pb-2 mb-3 border-b border-slate-800">
                  <span className="text-xs font-bold text-amber-400">
                    ✦ Сокровищница Алчности [Apotheosis NBT] ({observedArtifacts.length}/20) ✦
                  </span>
                  <button
                    onClick={() => setGreedCatalogOpen(false)}
                    className="text-xs text-slate-400 hover:text-white"
                  >
                    Закрыть [ESC]
                  </button>
                </div>
                <div className="space-y-2 max-h-44 overflow-y-auto pr-1">
                  {observedArtifacts.map((art) => (
                    <div
                      key={art.id}
                      className="flex items-center justify-between gap-2 p-2 rounded bg-slate-900 border border-slate-800 text-xs"
                    >
                      <div className="min-w-0">
                        <div className="font-medium text-amber-200 truncate">⚔ {art.name}</div>
                        <div className="text-[11px] text-slate-400 truncate">{art.enchantments}</div>
                      </div>
                      <button
                        onClick={() => {
                          if (currentMana < 30) {
                            setActionBarMessage('Недостаточно маны для репликации (требуется 30 ед.)!');
                            return;
                          }
                          setCurrentMana((m) => m - 30);
                          setReplicatedInventory((inv) => [art.name, ...inv.slice(0, 4)]);
                          setGreedCatalogOpen(false);
                          setActionBarMessage(
                            `ItemStack.of(tag): Создана копия со всеми аффиксами Apotheosis и самоцветами: ${art.name}!`
                          );
                        }}
                        className="px-2.5 py-1 bg-amber-500/20 hover:bg-amber-500/30 text-amber-300 border border-amber-500/40 rounded font-mono text-[11px] shrink-0 whitespace-nowrap"
                      >
                        Создать (-30 Маны)
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            ) : (
              <div className="w-full grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div className="p-3 rounded-lg bg-[#0B0F17]/80 border border-slate-800/90">
                  <div className="text-[11px] text-slate-400">Apotheosis &amp; Iron&apos;s Spells (Гнев)</div>
                  <div className="text-sm font-mono font-semibold text-rose-400 mt-1 tabular-nums">
                    +{apotheosisCritChance}% Крит · +{apotheosisArmorShred}% Пробой
                  </div>
                  <div className="text-[11px] text-slate-500 mt-0.5">
                    Spell Power: +{ironsSpellPowerBonus}% · Ранг Души: {dragonfyreSoulRank}
                  </div>
                </div>

                <div className="p-3 rounded-lg bg-[#0B0F17]/80 border border-slate-800/90">
                  <div className="text-[11px] text-slate-400">Украдено Завистью [Dragonfyre]</div>
                  <div className="text-sm font-mono font-semibold text-emerald-400 mt-1 truncate">
                    {stolenAbilityId || 'Нет'}
                  </div>
                  <div className="text-[11px] text-slate-500 mt-0.5 truncate">
                    Цель Похоти: {lustTargetName}
                  </div>
                </div>

                <div className="p-3 rounded-lg bg-[#0B0F17]/80 border border-slate-800/90">
                  <div className="text-[11px] text-slate-400">Статус Лени / Гордыни</div>
                  <div className="text-sm font-mono font-semibold text-sky-400 mt-1 tabular-nums">
                    {isSlothLocked
                      ? `Гибернация: ${slothLockedUntil - gameTimeTicks}т`
                      : isSlothAwakened
                      ? 'x5 ПРОБУЖДЕНИЕ КОРОЛЯ!'
                      : slothResting
                      ? 'Покой (+22 Мана/с, +5 HP/с)'
                      : 'Активен'}
                  </div>
                  <div className="text-[11px] text-slate-500 mt-0.5 tabular-nums">
                    КД Гордыни: {Math.max(0, prideCooldownUntil - gameTimeTicks)} тиков
                  </div>
                </div>
              </div>
            )}
          </div>

          {/* Bottom Minecraft Actionbar + SinsHudOverlay Preview */}
          <div className="space-y-3">
            <div className="text-center px-3 py-1.5 rounded bg-black/60 border border-slate-800 text-xs font-mono text-amber-300">
              {actionBarMessage}
            </div>

            {/* Authentic SinsHudOverlay Box */}
            <div className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 pt-2 border-t border-slate-800/80">
              <div className="bg-[#0B0F17] border border-slate-700/80 rounded-lg p-3 w-full sm:w-72">
                <div className="flex items-center justify-between text-xs font-semibold text-amber-400 mb-1.5">
                  <span>
                    {currentSin.titleRu} [Ранг {dragonfyreSoulRank}]
                  </span>
                  <span className="font-mono text-[11px] text-slate-400">Режим {activeSubMode + 1}</span>
                </div>

                {/* Mana Bar */}
                <div className="w-full h-2.5 bg-slate-800 rounded overflow-hidden mb-1">
                  <div
                    className="h-full bg-blue-500 transition-transform duration-150 origin-left"
                    style={{ transform: `scaleX(${Math.min(1, currentMana / Math.max(1, maxMana))})` }}
                  />
                </div>
                <div className="flex items-center justify-between text-[11px] font-mono tabular-nums text-slate-300">
                  <span>
                    Мана: {currentMana.toFixed(0)} / {maxMana.toFixed(0)}
                  </span>
                  <span className="text-rose-400">Гнев: {wrathStacks.toFixed(0)}%</span>
                </div>
              </div>

              {/* Keybind Interactive Buttons */}
              <div className="flex flex-wrap items-center gap-2">
                <button
                  onClick={() => {
                    setRadialMenuOpen((prev) => !prev);
                    setGreedCatalogOpen(false);
                  }}
                  className="px-3 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-mono font-medium text-slate-100 border border-slate-700 transition-colors whitespace-nowrap"
                >
                  [R] Меню Грехов
                </button>
                <button
                  onClick={cycleSubMode}
                  className="px-3 py-2 rounded-lg bg-slate-800 hover:bg-slate-700 text-xs font-mono font-medium text-slate-100 border border-slate-700 transition-colors whitespace-nowrap"
                >
                  [X] Сменить режим
                </button>
                <button
                  onClick={castActiveSin}
                  className="px-3.5 py-2 rounded-lg bg-amber-500 hover:bg-amber-400 text-xs font-mono font-semibold text-slate-950 transition-colors whitespace-nowrap"
                >
                  [V] Применить навык
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Dragonfyre Survival Event Triggers */}
        <div className="border border-slate-800 bg-[#0F172A] rounded-xl p-4">
          <div className="text-xs font-semibold text-slate-300 mb-3">
            Симуляция событий модпака Cisco&apos;s Fantasy Medieval RPG [Dragonfyre] (1.20.1)
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              onClick={() => {
                setUnlocked(true);
                setMaxMana((m) => m + 50);
                setCurrentMana(maxMana + 50);
                setDragonfyreSoulRank((r) => r + 1);
                setActionBarMessage(
                  'Съеден Запретный Плод Dragonfyre! +50 Макс. Маны, +1 Ранг Души, сброшены КД заклинаний Iron\'s Spells.'
                );
              }}
              className="px-3 py-1.5 rounded bg-purple-950/60 hover:bg-purple-900/70 border border-purple-700/50 text-purple-200 text-xs transition-colors flex items-center gap-1.5 whitespace-nowrap"
            >
              <Sparkles className="w-3.5 h-3.5" />
              Съесть Запретный Плод (+50 Маны)
            </button>

            <button
              onClick={() => {
                const next = Math.min(100, wrathStacks + 25);
                setWrathStacks(next);
                if (next >= 100) {
                  setWrathBerserkUntil(gameTimeTicks + 400);
                  setActionBarMessage(
                    'LivingHurtEvent: Стаки Гнева 100%! РЕЖИМ БЕРСЕРКА DRAGONFYRE (+25% Crit Chance, +35% Armor Shred Apotheosis)!'
                  );
                } else {
                  setActionBarMessage(
                    `LivingHurtEvent: Стаки Гнева ${next}% (Apotheosis Crit +${Math.floor(next / 10) * 4}%, Armor Shred +${Math.floor(next / 10) * 5}%).`
                  );
                }
              }}
              className="px-3 py-1.5 rounded bg-rose-950/60 hover:bg-rose-900/70 border border-rose-700/50 text-rose-200 text-xs transition-colors flex items-center gap-1.5 whitespace-nowrap"
            >
              <ShieldAlert className="w-3.5 h-3.5" />
              Удар в бою (+25% Гнева)
            </button>

            <button
              onClick={() => {
                setMaxMana((m) => m + 50);
                setCurrentMana((m) => Math.min(maxMana + 50, m + 100));
                setDragonfyreSoulRank((r) => r + 1);
                setStolenAbilityId('dragonfyre:lightning_dragon_breath');
                setActionBarMessage(
                  'Убит Дракон Ice & Fire / Босс Cataclysm! Поглощена душа Босса: +50 Max Mana, +1 Ранг Dragonfyre, сброшены КД Iron\'s Spells!'
                );
              }}
              className="px-3 py-1.5 rounded bg-amber-950/60 hover:bg-amber-900/70 border border-amber-700/50 text-amber-200 text-xs transition-colors flex items-center gap-1.5 whitespace-nowrap"
            >
              <Flame className="w-3.5 h-3.5" />
              Поглотить Душу Дракона (+50 Маны &amp; Ранг)
            </button>

            <button
              onClick={() => {
                setSlothResting((r) => !r);
                setActionBarMessage(
                  !slothResting
                    ? 'Лень (Покой): 5 сек без движения. Регенерация HP +5 ед./сек, Мана +22 ед./сек!'
                    : 'Вы начали движение — состояние Покоя Лени прервано.'
                );
              }}
              className={`px-3 py-1.5 rounded border text-xs transition-colors whitespace-nowrap ${
                slothResting
                  ? 'bg-sky-500/20 border-sky-400 text-sky-200'
                  : 'bg-slate-800 border-slate-700 text-slate-300 hover:bg-slate-700'
              }`}
            >
              {slothResting ? 'Покой Лени: АКТИВЕН (+22 Мана/с)' : 'Включить Покой Лени'}
            </button>
          </div>
        </div>
      </div>

      {/* Right 5 Columns: Live 1.20.1 CompoundTag & Dragonfyre Compat Inspector */}
      <div className="lg:col-span-5 flex flex-col gap-5">
        <div className="border border-slate-800 bg-[#0F172A] rounded-xl p-5 flex-1 flex flex-col justify-between">
          <div>
            <div className="flex items-center justify-between pb-3 mb-3 border-b border-slate-800">
              <div>
                <h3 className="text-sm font-semibold text-slate-100">
                  Инспектор Player.PERSISTED_NBT_TAG (1.20.1)
                </h3>
                <p className="text-xs text-slate-400 mt-0.5">
                  Кэш O(1) <code className="text-amber-300">PlayerSinsData</code> + Атрибуты Dragonfyre
                </p>
              </div>
              <span className="text-xs font-mono text-emerald-400">
                {nbtDirtySynced ? 'Dirty -> Synced' : 'Cached O(1)'}
              </span>
            </div>

            <pre className="p-3.5 rounded-lg bg-[#0B0F17] border border-slate-800/90 text-xs font-mono text-slate-200 overflow-x-auto leading-relaxed">
              {nbtDump}
            </pre>
          </div>

          <div className="mt-4 pt-4 border-t border-slate-800 space-y-2">
            <div className="text-xs font-semibold text-slate-300">
              Подрежимы выбранного греха ({currentSin.titleRu}):
            </div>
            <div className="space-y-1">
              {currentSin.modes.map((m, idx) => (
                <button
                  key={m}
                  onClick={() => setActiveSubMode(idx)}
                  className={`w-full text-left px-3 py-1.5 rounded text-xs font-mono transition-colors ${
                    idx === activeSubMode
                      ? 'bg-amber-500/15 text-amber-300 border border-amber-500/30'
                      : 'bg-slate-900/60 text-slate-400 hover:text-slate-200'
                  }`}
                >
                  {m}
                </button>
              ))}
            </div>
            {replicatedInventory.length > 0 && (
              <div className="pt-2 text-xs text-slate-400">
                <span className="text-amber-400 font-medium">Скопировано Алчностью: </span>
                {replicatedInventory.join(', ')}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
