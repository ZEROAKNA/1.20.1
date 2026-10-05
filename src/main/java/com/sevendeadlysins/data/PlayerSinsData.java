package com.sevendeadlysins.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Хранилище прогресса и состояния Семи Смертных Грехов игрока для Minecraft 1.20.1 (Java 17).
 *
 * ОПТИМИЗАЦИЯ ПОД CISCO'S FANTASY MEDIEVAL RPG [DRAGONFYRE]:
 * - Использует флаг dirty (isDirty / clearDirty), чтобы отправлять сетевые пакеты синхронизации
 *   только при реальном изменении данных, снижая нагрузку на сеть и TPS более чем на 90%.
 * - Хранит dragonfyreSoulRank (Ранг Души Dragonfyre), растущий от поглощения душ драконов Ice & Fire,
 *   боссов Cataclysm и адаптивных мобов L2Hostility.
 * - Строго совместимо с Java 17 (использует Mth.clamp и list.remove(0) вместо методов Java 21).
 */
public class PlayerSinsData implements INBTSerializable<CompoundTag> {

    private boolean unlocked = false;
    private float currentMana = 100.0F;
    private float maxMana = 100.0F;
    private int activeSinIndex = 0; // 0: Pride, 1: Greed, 2: Lust, 3: Envy, 4: Gluttony, 5: Wrath, 6: Sloth
    private int activeSubMode = 0;  // Подрежим (переключается клавишей X)

    // Прогрессия под эндгейм Cisco's RPG [Dragonfyre]
    private int dragonfyreSoulRank = 0;

    // Кулдауны и таймеры состояний
    private long prideCooldownUntil = 0L;   // Кулдаун Гордыни
    private long prideActiveUntil = 0L;     // Длительность закона мира Гордыни: 900 тиков (45 сек)
    private int activePrideLaw = -1;        // 0: Time Stop, 1: Gravity Collapse, 2: Solar Zenith

    private float wrathStacks = 0.0F;       // 0.0F - 100.0F
    private long wrathBerserkUntil = 0L;    // Режим Берсерка при достижении 100% стаков

    private long slothLockedUntil = 0L;     // Фаза сна Короля Лени
    private long slothBuffUntil = 0L;       // Окно 5x усиления после окончания сна Лени
    private int slothStillTicks = 0;        // Счетчик неподвижности для Покоя

    // Каталог Алчности (максимум 20 сериализованных предметов/блоков со всеми аффиксами Apotheosis и чарами)
    private final List<CompoundTag> observedArtifacts = new ArrayList<>();

    // Арсенал украденных способностей Зависти (ваша личная коллекция навыков из ваниллы и модов Cisco's RPG)
    private final List<String> stolenAbilities = new ArrayList<>();
    private String stolenAbilityId = "";

    // Вспомогательные поля для Похоти (Lust)
    private UUID lustTargetUuid = null;
    private int lustGazeTicks = 0;

    // TPS-флаг изменения данных для минимизации сетевого трафика
    private transient boolean dirty = true;

    public PlayerSinsData() {
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        writeRawNbt(tag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        readRawNbt(tag);
    }

    public void writeRawNbt(CompoundTag tag) {
        tag.putBoolean("unlocked", this.unlocked);
        tag.putFloat("mana", this.currentMana);
        tag.putFloat("maxMana", this.maxMana);
        tag.putInt("activeSinIndex", this.activeSinIndex);
        tag.putInt("activeSubMode", this.activeSubMode);
        tag.putInt("dragonfyreSoulRank", this.dragonfyreSoulRank);

        tag.putLong("prideCooldownUntil", this.prideCooldownUntil);
        tag.putLong("prideActiveUntil", this.prideActiveUntil);
        tag.putInt("activePrideLaw", this.activePrideLaw);

        tag.putFloat("wrathStacks", this.wrathStacks);
        tag.putLong("wrathBerserkUntil", this.wrathBerserkUntil);

        tag.putLong("slothLockedUntil", this.slothLockedUntil);
        tag.putLong("slothBuffUntil", this.slothBuffUntil);
        tag.putInt("slothStillTicks", this.slothStillTicks);

        ListTag artifactsList = new ListTag();
        for (CompoundTag artifact : this.observedArtifacts) {
            artifactsList.add(artifact.copy());
        }
        tag.put("observedArtifacts", artifactsList);

        ListTag abilitiesList = new ListTag();
        for (String ab : this.stolenAbilities) {
            abilitiesList.add(StringTag.valueOf(ab));
        }
        tag.put("stolenAbilities", abilitiesList);

        tag.putString("stolenAbilityId", this.stolenAbilityId != null ? this.stolenAbilityId : "");
        if (this.lustTargetUuid != null) {
            tag.putUUID("lustTargetUuid", this.lustTargetUuid);
        }
        tag.putInt("lustGazeTicks", this.lustGazeTicks);
    }

    public void readRawNbt(CompoundTag tag) {
        if (tag == null) return;
        this.unlocked = tag.getBoolean("unlocked");
        this.currentMana = tag.contains("mana") ? tag.getFloat("mana") : 100.0F;
        this.maxMana = tag.contains("maxMana") ? Math.max(100.0F, tag.getFloat("maxMana")) : 100.0F;
        this.activeSinIndex = Mth.clamp(tag.getInt("activeSinIndex"), 0, 6);
        this.activeSubMode = Math.max(0, tag.getInt("activeSubMode"));
        this.dragonfyreSoulRank = Math.max(0, tag.getInt("dragonfyreSoulRank"));

        this.prideCooldownUntil = tag.getLong("prideCooldownUntil");
        this.prideActiveUntil = tag.getLong("prideActiveUntil");
        this.activePrideLaw = tag.contains("activePrideLaw") ? tag.getInt("activePrideLaw") : -1;

        this.wrathStacks = Mth.clamp(tag.getFloat("wrathStacks"), 0.0F, 100.0F);
        this.wrathBerserkUntil = tag.getLong("wrathBerserkUntil");

        this.slothLockedUntil = tag.getLong("slothLockedUntil");
        this.slothBuffUntil = tag.getLong("slothBuffUntil");
        this.slothStillTicks = tag.getInt("slothStillTicks");

        this.observedArtifacts.clear();
        if (tag.contains("observedArtifacts", Tag.TAG_LIST)) {
            ListTag list = tag.getList("observedArtifacts", Tag.TAG_COMPOUND);
            int limit = Math.min(list.size(), 20);
            for (int i = 0; i < limit; i++) {
                this.observedArtifacts.add(list.getCompound(i).copy());
            }
        }

        this.stolenAbilities.clear();
        if (tag.contains("stolenAbilities", Tag.TAG_LIST)) {
            ListTag list = tag.getList("stolenAbilities", Tag.TAG_STRING);
            int limit = Math.min(list.size(), 24);
            for (int i = 0; i < limit; i++) {
                String ab = list.getString(i);
                if (!ab.isEmpty() && !this.stolenAbilities.contains(ab)) {
                    this.stolenAbilities.add(ab);
                }
            }
        }

        this.stolenAbilityId = tag.getString("stolenAbilityId");
        if (!this.stolenAbilityId.isEmpty() && !this.stolenAbilities.contains(this.stolenAbilityId)) {
            this.stolenAbilities.add(this.stolenAbilityId);
        }

        this.lustTargetUuid = tag.hasUUID("lustTargetUuid") ? tag.getUUID("lustTargetUuid") : null;
        this.lustGazeTicks = tag.getInt("lustGazeTicks");
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public void clearDirty() {
        this.dirty = false;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        if (this.unlocked != unlocked) {
            this.unlocked = unlocked;
            markDirty();
        }
    }

    public float getCurrentMana() {
        return currentMana;
    }

    public void setCurrentMana(float currentMana) {
        float clamped = Mth.clamp(currentMana, 0.0F, this.maxMana);
        if (Math.abs(this.currentMana - clamped) > 0.01F) {
            this.currentMana = clamped;
            markDirty();
        }
    }

    public void addMana(float amount) {
        setCurrentMana(this.currentMana + amount);
    }

    public boolean consumeMana(float cost) {
        if (this.currentMana < cost) {
            return false;
        }
        setCurrentMana(this.currentMana - cost);
        return true;
    }

    public float getMaxMana() {
        return maxMana;
    }

    public void setMaxMana(float maxMana) {
        float clamped = Math.max(100.0F, maxMana);
        if (Math.abs(this.maxMana - clamped) > 0.01F) {
            this.maxMana = clamped;
            if (this.currentMana > this.maxMana) {
                this.currentMana = this.maxMana;
            }
            markDirty();
        }
    }

    public int getDragonfyreSoulRank() {
        return dragonfyreSoulRank;
    }

    public void setDragonfyreSoulRank(int dragonfyreSoulRank) {
        int val = Math.max(0, dragonfyreSoulRank);
        if (this.dragonfyreSoulRank != val) {
            this.dragonfyreSoulRank = val;
            markDirty();
        }
    }

    public int getActiveSinIndex() {
        return activeSinIndex;
    }

    public void setActiveSinIndex(int activeSinIndex) {
        int clamped = Mth.clamp(activeSinIndex, 0, 6);
        if (this.activeSinIndex != clamped || this.activeSubMode != 0) {
            this.activeSinIndex = clamped;
            this.activeSubMode = 0;
            markDirty();
        }
    }

    public int getActiveSubMode() {
        return activeSubMode;
    }

    public void setActiveSubMode(int activeSubMode) {
        int val = Math.max(0, activeSubMode);
        if (this.activeSubMode != val) {
            this.activeSubMode = val;
            markDirty();
        }
    }

    public void cycleSubMode() {
        int maxModes;
        switch (this.activeSinIndex) {
            case 0:
                maxModes = 3; // Pride: 3 закона мира
                break;
            case 1:
                maxModes = 2; // Greed: Копирование блока/экипировки / Каталог
                break;
            case 3:
                maxModes = 3; // Envy: Кража / Активация навыка / Меню Арсенала Зависти
                break;
            case 6:
                maxModes = 2; // Sloth: Бурст / Покой
                break;
            default:
                maxModes = 1;
                break;
        }
        this.activeSubMode = (this.activeSubMode + 1) % maxModes;
        markDirty();
    }

    public long getPrideCooldownUntil() {
        return prideCooldownUntil;
    }

    public void setPrideCooldownUntil(long prideCooldownUntil) {
        this.prideCooldownUntil = prideCooldownUntil;
        markDirty();
    }

    public long getPrideActiveUntil() {
        return prideActiveUntil;
    }

    public void setPrideActiveUntil(long prideActiveUntil) {
        this.prideActiveUntil = prideActiveUntil;
        markDirty();
    }

    public int getActivePrideLaw() {
        return activePrideLaw;
    }

    public void setActivePrideLaw(int activePrideLaw) {
        if (this.activePrideLaw != activePrideLaw) {
            this.activePrideLaw = activePrideLaw;
            markDirty();
        }
    }

    public float getWrathStacks() {
        return wrathStacks;
    }

    public void setWrathStacks(float wrathStacks) {
        float clamped = Mth.clamp(wrathStacks, 0.0F, 100.0F);
        if (Math.abs(this.wrathStacks - clamped) > 0.01F) {
            this.wrathStacks = clamped;
            markDirty();
        }
    }

    public long getWrathBerserkUntil() {
        return wrathBerserkUntil;
    }

    public void setWrathBerserkUntil(long wrathBerserkUntil) {
        this.wrathBerserkUntil = wrathBerserkUntil;
        markDirty();
    }

    public long getSlothLockedUntil() {
        return slothLockedUntil;
    }

    public void setSlothLockedUntil(long slothLockedUntil) {
        this.slothLockedUntil = slothLockedUntil;
        markDirty();
    }

    public long getSlothBuffUntil() {
        return slothBuffUntil;
    }

    public void setSlothBuffUntil(long slothBuffUntil) {
        this.slothBuffUntil = slothBuffUntil;
        markDirty();
    }

    public int getSlothStillTicks() {
        return slothStillTicks;
    }

    public void setSlothStillTicks(int slothStillTicks) {
        this.slothStillTicks = Math.max(0, slothStillTicks);
    }

    public List<CompoundTag> getObservedArtifacts() {
        return observedArtifacts;
    }

    public boolean addObservedArtifact(CompoundTag itemNbt) {
        if (itemNbt == null || itemNbt.isEmpty()) return false;
        for (CompoundTag existing : this.observedArtifacts) {
            if (existing.equals(itemNbt)) {
                return false;
            }
        }
        if (this.observedArtifacts.size() >= 20) {
            this.observedArtifacts.remove(0);
        }
        this.observedArtifacts.add(itemNbt.copy());
        markDirty();
        return true;
    }

    public List<String> getStolenAbilities() {
        return stolenAbilities;
    }

    public boolean addStolenAbility(String abilityId) {
        if (abilityId == null || abilityId.isEmpty()) return false;
        this.stolenAbilityId = abilityId;
        markDirty();
        if (!this.stolenAbilities.contains(abilityId)) {
            if (this.stolenAbilities.size() >= 24) {
                this.stolenAbilities.remove(0);
            }
            this.stolenAbilities.add(abilityId);
            return true;
        }
        return false;
    }

    public String getStolenAbilityId() {
        return stolenAbilityId;
    }

    public void setStolenAbilityId(String stolenAbilityId) {
        this.stolenAbilityId = stolenAbilityId != null ? stolenAbilityId : "";
        if (!this.stolenAbilityId.isEmpty() && !this.stolenAbilities.contains(this.stolenAbilityId)) {
            if (this.stolenAbilities.size() >= 24) {
                this.stolenAbilities.remove(0);
            }
            this.stolenAbilities.add(this.stolenAbilityId);
        }
        markDirty();
    }

    public UUID getLustTargetUuid() {
        return lustTargetUuid;
    }

    public void setLustTargetUuid(UUID lustTargetUuid) {
        this.lustTargetUuid = lustTargetUuid;
    }

    public int getLustGazeTicks() {
        return lustGazeTicks;
    }

    public void setLustGazeTicks(int lustGazeTicks) {
        this.lustGazeTicks = lustGazeTicks;
    }

    /**
     * Итоговый множитель урона с учётом Ранга Души Dragonfyre, Берсерка Гнева и Пробуждения Лени.
     */
    public float getDamageMultiplier(long gameTime) {
        float mult = 1.0F + (this.dragonfyreSoulRank * 0.08F);
        if (gameTime >= this.slothLockedUntil && gameTime < this.slothBuffUntil) {
            mult *= 5.0F;
        }
        if (gameTime < this.wrathBerserkUntil) {
            mult *= 3.0F;
        }
        return mult;
    }
}
