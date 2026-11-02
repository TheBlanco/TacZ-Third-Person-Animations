package com.skyplayer.tacztpanim.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import java.util.Optional;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RemoteGunContext extends GunAnimationStateContext {
    private final Player target;
    private ItemStack gunItem = ItemStack.EMPTY;
    private float walkDistAnchor;
    long lastShootTimestamp = -1L;

    public RemoteGunContext(Player target) {
        this.target = target;
    }

    @Override
    public void setCurrentGunItem(ItemStack currentGunItem) {
        super.setCurrentGunItem(currentGunItem);
        this.gunItem = currentGunItem;
    }

    private IGunOperator operator() {
        return IGunOperator.fromLivingEntity(this.target);
    }

    private Optional<GunData> gunData() {
        IGun iGun = IGun.getIGunOrNull(this.gunItem);
        return iGun == null ? Optional.empty() : TimelessAPI.getClientGunIndex(iGun.getGunId(this.gunItem)).map(ClientGunIndex::getGunData);
    }

    @Override
    public long getShootInterval() {
        IGun iGun = IGun.getIGunOrNull(this.gunItem);
        if (iGun == null) {
            return 0L;
        } else {
            FireMode fireMode = iGun.getFireMode(this.gunItem);
            return this.gunData()
                .map(
                    data -> fireMode == FireMode.BURST
                        ? (long)(data.getBurstData().getMinInterval() * 1000.0)
                        : data.getShootInterval(this.target, fireMode, this.gunItem)
                )
                .map(v -> Math.max(v, 0L))
                .orElse(0L);
        }
    }

    @Override
    public long getLastShootTimestamp() {
        return this.lastShootTimestamp;
    }

    @Override
    public void adjustClientShootInterval(long alpha) {
    }

    @Override
    public boolean hasAmmoToConsume() {
        return true;
    }

    @Override
    public float getAimingProgress() {
        return this.operator().getSynAimingProgress();
    }

    @Override
    public boolean isAiming() {
        return this.operator().getSynIsAiming();
    }

    @Override
    public long getShootCoolDown() {
        return 0L;
    }

    @Override
    public int getReloadStateType() {
        return this.operator().getSynReloadState().getStateType().ordinal();
    }

    @Override
    public boolean isInputUp() {
        return false;
    }

    @Override
    public boolean isInputDown() {
        return false;
    }

    @Override
    public boolean isInputLeft() {
        return false;
    }

    @Override
    public boolean isInputRight() {
        return false;
    }

    @Override
    public boolean isInputJumping() {
        return false;
    }

    @Override
    public boolean isCrawl() {
        return this.target.isVisuallyCrawling();
    }

    @Override
    public boolean isOnGround() {
        return this.target.onGround();
    }

    @Override
    public boolean isCrouching() {
        return this.target.isCrouching();
    }

    @Override
    public boolean shouldSlide() {
        return this.target.isCrouching() && this.gunData().map(GunData::canSlide).orElse(false);
    }

    @Override
    public void anchorWalkDist() {
        this.walkDistAnchor = this.currentWalkDist();
    }

    @Override
    public float getWalkDist() {
        return this.currentWalkDist() - this.walkDistAnchor;
    }

    private float currentWalkDist() {
        return this.target.walkDist + (this.target.walkDist - this.target.walkDistO) * this.partialTicks;
    }

    @Override
    public void popShellFrom(int index) {
    }

    @Override
    public float getChargeProgress() {
        return 0.0F;
    }

    @Override
    public boolean isCharging() {
        return false;
    }
}
