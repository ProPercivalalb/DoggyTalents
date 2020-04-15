package doggytalents.entity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

import com.google.common.base.Optional;

import doggytalents.DoggyTalents;
import doggytalents.ModBlocks;
import doggytalents.ModItems;
import doggytalents.ModSerializers;
import doggytalents.ModTalents;
import doggytalents.api.DoggyTalentsAPI;
import doggytalents.api.feature.EnumGender;
import doggytalents.api.feature.EnumMode;
import doggytalents.api.feature.ICoordFeature;
import doggytalents.api.feature.IGenderFeature;
import doggytalents.api.feature.IHungerFeature;
import doggytalents.api.feature.ILevelFeature;
import doggytalents.api.feature.IModeFeature;
import doggytalents.api.feature.IStatsFeature;
import doggytalents.api.feature.ITalentFeature;
import doggytalents.api.inferface.IDogEntity;
import doggytalents.api.inferface.IDogItem;
import doggytalents.api.inferface.IThrowableItem;
import doggytalents.api.inferface.Talent;
import doggytalents.api.lib.Reference;
import doggytalents.entity.ai.DogLocationManager;
import doggytalents.entity.ai.EntityAIBegDog;
import doggytalents.entity.ai.EntityAIBerserkerMode;
import doggytalents.entity.ai.EntityAIDogFeed;
import doggytalents.entity.ai.EntityAIDogWander;
import doggytalents.entity.ai.EntityAIExtinguishFire;
import doggytalents.entity.ai.EntityAIFetch;
import doggytalents.entity.ai.EntityAIFetchReturn;
import doggytalents.entity.ai.EntityAIFollowOwnerDog;
import doggytalents.entity.ai.EntityAIHurtByTargetDog;
import doggytalents.entity.ai.EntityAIIncapacitatedTargetDog;
import doggytalents.entity.ai.EntityAIOwnerHurtByTargetDog;
import doggytalents.entity.ai.EntityAIOwnerHurtTargetDog;
import doggytalents.entity.ai.EntityAIShepherdDog;
import doggytalents.entity.features.CoordFeature;
import doggytalents.entity.features.DogFeature;
import doggytalents.entity.features.DogStats;
import doggytalents.entity.features.GenderFeature;
import doggytalents.entity.features.HungerFeature;
import doggytalents.entity.features.LevelFeature;
import doggytalents.entity.features.ModeFeature;
import doggytalents.entity.features.TalentFeature;
import doggytalents.helper.DogUtil;
import doggytalents.helper.TalentHelper;
import doggytalents.inventory.InventoryTreatBag;
import doggytalents.item.ItemChewStick;
import doggytalents.item.ItemFancyCollar;
import doggytalents.lib.ConfigValues;
import doggytalents.lib.GuiNames;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAISit;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ITeleporter;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author ProPercivalalb
 */
public class EntityDog extends IDogEntity {

    private static final DataParameter<Float>                    DATA_HEALTH_ID  = EntityDataManager.createKey(EntityDog.class, DataSerializers.FLOAT);
    private static final DataParameter<Byte>                     DOG_TEXTURE     = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Integer>                  COLLAR_COLOUR   = EntityDataManager.createKey(EntityDog.class, DataSerializers.VARINT);
    private static final DataParameter<Byte>                     LEVEL           = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Byte>                     LEVEL_DIRE      = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Byte>                     DOG_FLAGS       = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Map<Talent, Integer>>     TALENTS_PARAM   = EntityDataManager.createKey(EntityDog.class, ModSerializers.TALENT_LEVEL_SERIALIZER);
    private static final DataParameter<Integer>                  HUNGER_PARAM    = EntityDataManager.createKey(EntityDog.class, DataSerializers.VARINT);
    private static final DataParameter<ItemStack>                BONE_VARIANT    = EntityDataManager.createKey(EntityDog.class, DataSerializers.ITEM_STACK);
    private static final DataParameter<Integer>                  CAPE            = EntityDataManager.createKey(EntityDog.class, DataSerializers.VARINT);
    private static final DataParameter<Optional<BlockPos>>       BOWL_POS        = EntityDataManager.createKey(EntityDog.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Optional<BlockPos>>       BED_POS         = EntityDataManager.createKey(EntityDog.class, DataSerializers.OPTIONAL_BLOCK_POS);
    private static final DataParameter<Byte>                     SIZE            = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Byte>                     GENDER_PARAM    = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Byte>                     MODE_PARAM      = EntityDataManager.createKey(EntityDog.class, DataSerializers.BYTE);
    private static final DataParameter<Optional<ITextComponent>>LAST_KNOWN_NAME = EntityDataManager.createKey(EntityDog.class, ModSerializers.OPTIONAL_TEXT_COMPONENT_SERIALIZER);

    public DogLocationManager locationManager;

    public TalentFeature TALENTS;
    public LevelFeature LEVELS;
    public ModeFeature MODE;
    public CoordFeature COORDS;
    public GenderFeature GENDER;
    public DogStats STATS;
    public HungerFeature HUNGER;
    private List<DogFeature> FEATURES;

    public Map<String, Object> objects;

    private float headRotationCourse;
    private float headRotationCourseOld;
    public boolean isWet;
    public boolean gotWetInWater;
    private boolean isShaking;
    private float timeDogIsShaking;
    private float prevTimeDogIsShaking;

    private float timeWolfIsHappy;
    private float prevTimeWolfIsHappy;
    private boolean isWolfHappy;
    public boolean hiyaMaster;
    private int reversionTime;
    private int hungerTick;
    private int prevHungerTick;
    private int healingTick;
    private int prevHealingTick;
    private int regenerationTick;
    private int prevRegenerationTick;

    public EntityDog(World world) {
        super(world);
        this.TALENTS = new TalentFeature(this);
        this.LEVELS = new LevelFeature(this);
        this.MODE = new ModeFeature(this);
        this.COORDS = new CoordFeature(this);
        this.GENDER = new GenderFeature(this);
        this.STATS = new DogStats(this);
        this.HUNGER = new HungerFeature(this);
        this.FEATURES = Arrays.asList(TALENTS, LEVELS, MODE, COORDS, GENDER, STATS, HUNGER);
        if(!this.world.isRemote) {
            this.locationManager = DogLocationManager.getHandler(this.getEntityWorld());
        }
        this.objects = new HashMap<String, Object>();
        this.setSize(0.6F, 0.85F);
        this.setTamed(false);
        this.setGender(this.getRNG().nextBoolean() ? EnumGender.MALE : EnumGender.FEMALE);

        TalentHelper.onClassCreation(this);
    }

    @Override
    protected void initEntityAI() {
        this.aiSit = new EntityAISit(this);
        this.tasks.addTask(1, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIExtinguishFire(this, 1.15D, 16));
        this.tasks.addTask(2, this.aiSit);
        this.tasks.addTask(3, new EntityAIFetchReturn(this, 1.0D));
        this.tasks.addTask(4, new EntityAIDogWander(this, 1.0D));
        this.tasks.addTask(5, new EntityAILeapAtTarget(this, 0.4F));
        this.tasks.addTask(6, new EntityAIAttackMelee(this, 1.0D, true));
        this.tasks.addTask(7, new EntityAIShepherdDog(this, 1.0D, 8F, entity -> !(entity instanceof EntityDog)));
        this.tasks.addTask(8, new EntityAIFetch(this, 1.0D, 32));
        this.tasks.addTask(10, new EntityAIFollowOwnerDog(this, 1.0D, 10.0F, 2.0F));
        //this.tasks.addTask(11, new EntityAISitOnBed(this, 0.8D));

        this.tasks.addTask(12, new EntityAIMate(this, 1.0D));
        this.tasks.addTask(13, new EntityAIWanderAvoidWater(this, 1.0D));
        this.tasks.addTask(14, new EntityAIBegDog(this, 8.0F));
        this.tasks.addTask(15, new EntityAIDogFeed(this, 1.0D, 20.0F));
        this.tasks.addTask(25, new EntityAIWatchClosest(this, EntityPlayer.class, 8.0F));
        this.tasks.addTask(25, new EntityAILookIdle(this));

        this.targetTasks.addTask(0, new EntityAIIncapacitatedTargetDog(this));
        this.targetTasks.addTask(1, new EntityAIOwnerHurtByTargetDog(this));
        this.targetTasks.addTask(2, new EntityAIOwnerHurtTargetDog(this));
        this.targetTasks.addTask(3, new EntityAIHurtByTargetDog(this, true));
        this.targetTasks.addTask(4, new EntityAIBerserkerMode<>(this, EntityMob.class, false));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(DATA_HEALTH_ID, this.getHealth());
        this.dataManager.register(DOG_FLAGS, (byte)0);
        this.dataManager.register(DOG_TEXTURE, (byte)0);
        this.dataManager.register(COLLAR_COLOUR, -2);
        this.dataManager.register(TALENTS_PARAM, Collections.emptyMap());
        this.dataManager.register(HUNGER_PARAM, 60);
        this.dataManager.register(BONE_VARIANT, ItemStack.EMPTY);
        this.dataManager.register(MODE_PARAM, (byte)EnumMode.DOCILE.getIndex());
        this.dataManager.register(LEVEL, (byte)0);
        this.dataManager.register(LEVEL_DIRE, (byte)0);
        this.dataManager.register(BOWL_POS, Optional.absent());
        this.dataManager.register(BED_POS, Optional.absent());
        this.dataManager.register(CAPE, -2);
        this.dataManager.register(SIZE, (byte)3);
        this.dataManager.register(GENDER_PARAM, (byte)EnumGender.UNISEX.getIndex());
        this.dataManager.register(LAST_KNOWN_NAME, Optional.absent());
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> key) {
        super.notifyDataManagerChange(key);
        if(SIZE.equals(key)) {
            this.setScale(this.getDogSize() * 0.3F + 0.1F);
        }
    }

    @Override
    protected void updateAITasks() {
        super.updateAITasks();
        this.dataManager.set(DATA_HEALTH_ID, this.getHealth());
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.3F);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(this.isTamed() ? 20.0D : 8.0D);
        this.getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.0D);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if(this.getDogHunger() <= ConfigValues.LOW_HUNGER && ConfigValues.DOG_WHINE_WHEN_HUNGER_LOW) {
            return SoundEvents.ENTITY_WOLF_WHINE;
        } else if(this.rand.nextInt(3) == 0) {
            return this.isTamed() && this.dataManager.get(DATA_HEALTH_ID) < this.getMaxHealth() / 2 ? SoundEvents.ENTITY_WOLF_WHINE : SoundEvents.ENTITY_WOLF_PANT;
        } else {
            return SoundEvents.ENTITY_WOLF_AMBIENT;
        }
    }

    @Override
    protected void playStepSound(BlockPos pos, Block blockIn) {
        this.playSound(SoundEvents.ENTITY_WOLF_STEP, 0.15F, 1.0F);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSourceIn) {
        return SoundEvents.ENTITY_WOLF_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_WOLF_DEATH;
    }

    @Override
    public float getSoundVolume() {
        return 0.4F;
    }

    public static final ResourceLocation DOG_LOOT_TABLE = LootTableList.register(new ResourceLocation(Reference.MOD_ID, "entities/dog"));

    @Override
    protected ResourceLocation getLootTable() {
        return DOG_LOOT_TABLE;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        this.FEATURES.forEach(f -> f.writeAdditional(compound));

        compound.setInteger("doggyTex", this.getTameSkin());
        compound.setInteger("collarColour", this.getCollarData());
        compound.setInteger("dogHunger", this.getDogHunger());
        compound.setBoolean("willObey", this.willObeyOthers());
        compound.setBoolean("friendlyFire", this.canPlayersAttack());
        compound.setBoolean("radioCollar", this.hasRadarCollar());
        compound.setBoolean("sunglasses", this.hasSunglasses());
        compound.setInteger("capeData", this.getCapeData());
        compound.setInteger("dogSize", this.getDogSize());
        compound.setBoolean("hasBone", this.hasBone());
        if(this.hasBone()) {
            compound.setTag("fetchItem", this.getBoneVariant().writeToNBT(new NBTTagCompound()));
        }
        if(this.dataManager.get(LAST_KNOWN_NAME).isPresent()) compound.setString("lastKnownOwnerName", ITextComponent.Serializer.componentToJson(this.dataManager.get(LAST_KNOWN_NAME).get()));

        TalentHelper.writeAdditional(this, compound);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.FEATURES.forEach(f -> f.readAdditional(compound));

        this.setTameSkin(compound.getInteger("doggyTex"));
        if (compound.hasKey("collarColour", 99)) this.setCollarData(compound.getInteger("collarColour"));
        this.setDogHunger(compound.getInteger("dogHunger"));
        this.setWillObeyOthers(compound.getBoolean("willObey"));
        this.setCanPlayersAttack(compound.getBoolean("friendlyFire"));
        this.hasRadarCollar(compound.getBoolean("radioCollar"));
        this.setHasSunglasses(compound.getBoolean("sunglasses"));
        if(compound.hasKey("capeData", 99)) this.setCapeData(compound.getInteger("capeData"));
        if(compound.hasKey("dogSize", 99)) this.setDogSize(compound.getInteger("dogSize"));

        if(compound.hasKey("fetchItem", Constants.NBT.TAG_COMPOUND)) this.setBoneVariant(new ItemStack(compound.getCompoundTag("fetchItem")));
        if(compound.hasKey("lastKnownOwnerName", 8)) this.dataManager.set(LAST_KNOWN_NAME, Optional.of(ITextComponent.Serializer.jsonToComponent(compound.getString("lastKnownOwnerName"))));

        TalentHelper.readAdditional(this, compound);

        //Backwards Compatibility
        if (compound.hasKey("dogName"))
            this.setCustomNameTag(compound.getString("dogName"));

        if(compound.getBoolean("hasBone")) {
            int variant = compound.getInteger("boneVariant");
            if(variant == 0) {
                this.setBoneVariant(new ItemStack(ModItems.THROW_BONE));
            } else if(variant == 1) {
                this.setBoneVariant(new ItemStack(ModItems.THROW_STICK));
            }
        }
    }

    public EntityAISit getSitAI() {
        return this.aiSit;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();

        if(!this.world.isRemote && this.isWet && !this.isShaking && !this.hasPath() && this.onGround) {
            this.isShaking = true;
            this.timeDogIsShaking = 0.0F;
            this.prevTimeDogIsShaking = 0.0F;
            this.world.setEntityState(this, (byte)8);
        }

        if(ConfigValues.IS_HUNGER_ON) {
            this.prevHungerTick = this.hungerTick;

            if(!this.isBeingRidden() && !this.isSitting() /** && !this.mode.isMode(EnumMode.WANDERING) && !this.level.isDireDog() || worldObj.getWorldInfo().getWorldTime() % 2L == 0L **/)
                this.hungerTick += 1;

            this.hungerTick += TalentHelper.hungerTick(this, this.hungerTick - this.prevHungerTick);

            if(this.hungerTick > 400) {
                this.setDogHunger(this.getDogHunger() - 1);
                this.hungerTick -= 400;
            }
        }

        if(ConfigValues.DOGS_IMMORTAL) {
            this.prevRegenerationTick = this.regenerationTick;

            if(this.isSitting()) {
                this.regenerationTick += 1;
                this.regenerationTick += TalentHelper.regenerationTick(this, this.regenerationTick - this.prevRegenerationTick);
            } else if(!this.isSitting())
                this.regenerationTick = 0;

            if(this.regenerationTick >= 2400 && this.isIncapacicated()) {
                this.setHealth(2);
                this.setDogHunger(1);
            } else if(this.regenerationTick >= 2400 && !this.isIncapacicated()) {
                if(this.regenerationTick >= 4400 && this.getDogHunger() < 60) {
                    this.setDogHunger(this.getDogHunger() + 1);
                    this.world.setEntityState(this, (byte) 7);
                    this.regenerationTick = 2400;
                }
            }
        }

        if(this.getHealth() != ConfigValues.LOW_HEATH_LEVEL) {
            this.prevHealingTick = this.healingTick;
            this.healingTick += this.nourishment();

            if(this.healingTick >= 6000) {
                if(this.getHealth() < this.getMaxHealth())
                    this.setHealth(this.getHealth() + 1);

                this.healingTick = 0;
            }
        }

        if(this.getHealth() <= 0 && this.isImmortal()) {
            this.deathTime = 0;
            this.setHealth(1);
        }

        if(this.LEVELS.isDireDog() && ConfigValues.DIRE_PARTICLES)
            for(int i = 0; i < 2; i++)
                this.world.spawnParticle(EnumParticleTypes.PORTAL, this.posX + (this.rand.nextDouble() - 0.5D) * this.width, (this.posY + rand.nextDouble() * height) - 0.25D, posZ + (rand.nextDouble() - 0.5D) * this.width, (this.rand.nextDouble() - 0.5D) * 2D, -this.rand.nextDouble(), (this.rand.nextDouble() - 0.5D) * 2D);

        if(this.reversionTime > 0)
            this.reversionTime -= 1;

        //Remove dog from players head if sneaking
        Entity entityRidden = this.getRidingEntity();

        if (entityRidden instanceof EntityPlayer)
            if (entityRidden.isSneaking())
                this.dismountRidingEntity();

        //Check if dog bowl still exists every 50t/2.5s, if not remove
        if(this.ticksExisted % 50 == 0) {
            if(this.COORDS.hasBowlPos() && this.world.isBlockLoaded(this.COORDS.getBowlPos()) && this.world.getBlockState(this.COORDS.getBowlPos()).getBlock() != ModBlocks.FOOD_BOWL) {
                this.COORDS.resetBowlPosition();
            }
        }

        TalentHelper.livingTick(this);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        this.headRotationCourseOld = this.headRotationCourse;
        if (this.isBegging()) {
            this.headRotationCourse += (1.0F - this.headRotationCourse) * 0.4F;
        }
        else {
            this.headRotationCourse += (0.0F - this.headRotationCourse) * 0.4F;
        }

        if(this.isWet()) {
            this.isWet = true;
            this.isShaking = false;
            this.timeDogIsShaking = 0.0F;
            this.prevTimeDogIsShaking = 0.0F;
            this.gotWetInWater = this.isInWater();
        } else if((this.isWet || this.isShaking) && this.isShaking) {
            if(this.timeDogIsShaking == 0.0F) {
                this.playSound(SoundEvents.ENTITY_WOLF_SHAKE, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            }

            this.prevTimeDogIsShaking = this.timeDogIsShaking;
            this.timeDogIsShaking += 0.05F;
            if(this.prevTimeDogIsShaking >= 2.0F) {
                this.isWet = false;
                this.isShaking = false;
                this.prevTimeDogIsShaking = 0.0F;
                this.timeDogIsShaking = 0.0F;

                TalentHelper.onFinishShaking(this, this.gotWetInWater);
            }

            if(this.timeDogIsShaking > 0.4F) {
                float f = (float)this.getEntityBoundingBox().minY;
                int i = (int)(MathHelper.sin((this.timeDogIsShaking - 0.4F) * (float)Math.PI) * 7.0F);

                for(int j = 0; j < i; ++j) {
                    float f1 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
                    float f2 = (this.rand.nextFloat() * 2.0F - 1.0F) * this.width * 0.5F;
                    this.world.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX + f1, f + 0.8F, this.posZ + f2, this.motionX, this.motionY, this.motionZ);
                }
            }
        }

        if (this.rand.nextInt(200) == 0)
            this.hiyaMaster = true;

        if (((this.isBegging()) || (this.hiyaMaster)) && (!this.isWolfHappy)) {
            this.isWolfHappy = true;
            this.timeWolfIsHappy = 0.0F;
            this.prevTimeWolfIsHappy = 0.0F;
        } else
            this.hiyaMaster = false;

        if (this.isWolfHappy) {
            if (this.timeWolfIsHappy % 1.0F == 0.0F)
                this.playSound(SoundEvents.ENTITY_WOLF_PANT, this.getSoundVolume(), (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1.0F);
            this.prevTimeWolfIsHappy = this.timeWolfIsHappy;
            this.timeWolfIsHappy += 0.05F;
            if (this.prevTimeWolfIsHappy >= 8.0F) {
                this.isWolfHappy = false;
                this.prevTimeWolfIsHappy = 0.0F;
                this.timeWolfIsHappy = 0.0F;
            }
        }

        if(this.isTamed()) {
            EntityPlayer player = (EntityPlayer) this.getOwner();

            if(player != null) {
                float distanceToOwner = player.getDistance(this);

                if(distanceToOwner <= 2F && this.hasBone()) {
                    if(!this.world.isRemote) {
                        IThrowableItem throwableItem = this.getThrowableItem();
                        ItemStack fetchItem = throwableItem != null ? throwableItem.getReturnStack(this.getBoneVariant()) : this.getBoneVariant();

                        this.entityDropItem(fetchItem, 0.0F);
                        this.setBoneVariant(ItemStack.EMPTY);
                    }
                }
            }
        }

        if(this.ticksExisted % 40 == 0) {
            if(!this.world.isRemote) {
                if(this.isEntityAlive()) { //Prevent the data from being added when the entity dies
                    this.locationManager.update(this);
                } else {
                    this.locationManager.remove(this);
                }


                if(this.getOwner() != null)
                    this.dataManager.set(LAST_KNOWN_NAME, Optional.fromNullable(this.getOwner().getDisplayName()));
            }
        }

        this.FEATURES.forEach(DogFeature::tick);
        TalentHelper.tick(this);
    }

    public boolean isControllingPassengerPlayer() {
        return this.getControllingPassenger() instanceof EntityPlayer;
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        if(!TalentHelper.isImmuneToFalls(this))
            super.fall(distance - TalentHelper.fallProtection(this), damageMultiplier);
    }

    @Override
    public boolean attackEntityFrom(DamageSource damageSource, float damage) {
        if (this.isEntityInvulnerable(damageSource))
            return false;
        else {
            Entity entity = damageSource.getTrueSource();
            //Friendly fire
            if (!this.canPlayersAttack() && entity instanceof EntityPlayer)
                return false;

            if (!TalentHelper.attackEntityFrom(this, damageSource, damage))
                return false;

            if (this.aiSit != null)
                this.aiSit.setSitting(false);

            if (entity != null && !(entity instanceof EntityPlayer) && !(entity instanceof EntityArrow))
                damage = (damage + 1.0F) / 2.0F;

            return super.attackEntityFrom(damageSource, damage);
        }
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if(!TalentHelper.shouldDamageMob(this, entity))
            return false;

        int damage = 4 + (MathHelper.floor(this.effectiveLevel()) + 1) / 2;
        damage = TalentHelper.attackEntityAsMob(this, entity, damage);

        if (entity instanceof EntityZombie)
            ((EntityZombie) entity).setAttackTarget(this);

        boolean flag = entity.attackEntityFrom(DamageSource.causeMobDamage(this), damage);//(float)((int)this.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue()));
        if(flag) {
            this.STATS.increaseDamageDealt(damage);
            this.applyEnchantments(this, entity);
        }

        return flag;

    }

    @Override
    public void setTamed(boolean tamed) {
        super.setTamed(tamed);
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(tamed ? 20.0D : 8.0D);
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4.0D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte id) {
        if(id == 8) {
            this.isShaking = true;
            this.timeDogIsShaking = 0.0F;
            this.prevTimeDogIsShaking = 0.0F;
        } else {
            super.handleStatusUpdate(id);
        }
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        EnumActionResult result = TalentHelper.interactWithPlayer(this, player, hand);
        switch(result) {
            case SUCCESS: return true;
            case FAIL: return false;
            case PASS: break;
        }

        ItemStack stack = player.getHeldItem(hand);

        if(stack.getItem() == ModItems.OWNER_CHANGE && player.capabilities.isCreativeMode && !this.isOwner(player)) {
            if(!this.world.isRemote) {
                this.setTamed(true);
                this.navigator.clearPath();
                this.setAttackTarget((EntityLivingBase) null);
                this.aiSit.setSitting(true);
                this.setOwnerId(player.getUniqueID());
                this.playTameEffect(true);
                this.world.setEntityState(this, (byte) 7);
            }
            return true;
        }

        if(this.isTamed()) {
            if(!stack.isEmpty()) {
                int foodValue = this.foodValue(stack);

                if(foodValue != 0 && this.getDogHunger() < ConfigValues.HUNGER_POINTS && this.canInteract(player)) {
                    if(this.isIncapacicated()) {
                        if(!this.world.isRemote)
                            player.sendMessage(new TextComponentTranslation("dog.mode.incapacitated.help", this.getDisplayName(), this.GENDER.getGenderPronoun()));
                    } else {
                        this.consumeItemFromStack(player, stack);

                        if(!this.world.isRemote) {
                            this.setDogHunger(this.getDogHunger() + foodValue);
                            if(stack.getItem() == ModItems.CHEW_STICK)
                                ((ItemChewStick)ModItems.CHEW_STICK).addChewStickEffects(this);

                        }
                        this.playTameEffect(true);
                    }
                    return true;
                }
                else if(stack.getItem() == ModItems.DOGGY_CHARM && player.capabilities.isCreativeMode) {
                    if(!this.world.isRemote) {
                        EntityDog babySpawn = this.createChild(this);
                        if(babySpawn != null) {
                           babySpawn.setGrowingAge(-ConfigValues.TIME_TO_MATURE);
                           babySpawn.setTamed(true);
                           if(ConfigValues.PUPS_GET_PARENT_LEVELS) {
                               babySpawn.LEVELS.setLevel(Math.min(this.LEVELS.getLevel(), 20));
                           }

                           babySpawn.setLocationAndAngles(this.posX, this.posY, this.posZ, 0.0F, 0.0F);
                           this.world.spawnEntity(babySpawn);

                           this.consumeItemFromStack(player, stack);
                        }
                     }

                    return true;
                }
                else if(stack.getItem() == Items.STICK && this.canInteract(player)) {

                    if(this.isIncapacicated()) {
                        if(!this.world.isRemote)
                            player.sendMessage(new TextComponentTranslation("dog.mode.incapacitated.help", this.getDisplayName(), this.GENDER.getGenderPronoun()));
                    } else {
                         player.openGui(DoggyTalents.INSTANCE, GuiNames.GUI_ID_DOGGY, this.world, this.getEntityId(), MathHelper.floor(this.posY), MathHelper.floor(this.posZ));
                    }

                    return true;
                } else if(stack.getItem() == ModItems.RADIO_COLLAR && this.canInteract(player) && !this.hasRadarCollar() && !this.isIncapacicated()) {
                    this.hasRadarCollar(true);

                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() == ModItems.WOOL_COLLAR && this.canInteract(player) && !this.hasCollar() && !this.isIncapacicated()) {
                    int colour = -1;

                    if(stack.hasTagCompound() && stack.getTagCompound().hasKey("collar_colour", 99))
                        colour = stack.getTagCompound().getInteger("collar_colour");

                    this.setCollarData(colour);

                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() instanceof ItemFancyCollar && this.canInteract(player) && !this.hasCollar() && !this.isIncapacicated()) {
                    this.setCollarData(-3 - ((ItemFancyCollar)stack.getItem()).type.ordinal());

                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() == ModItems.CAPE && this.canInteract(player) && !this.hasCape() && !this.isIncapacicated()) {
                    this.setFancyCape();
                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() == ModItems.LEATHER_JACKET && this.canInteract(player) && !this.hasCape() && !this.isIncapacicated()) {
                    this.setLeatherJacket();
                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() == ModItems.CAPE_COLOURED && this.canInteract(player) && !this.hasCape() && !this.isIncapacicated()) {
                    int colour = -1;

                    if(stack.hasTagCompound() && stack.getTagCompound().hasKey("cape_colour", 99))
                        colour = stack.getTagCompound().getInteger("cape_colour");

                    this.setCapeData(colour);

                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() == ModItems.SUNGLASSES && this.canInteract(player) && !this.hasSunglasses() && !this.isIncapacicated()) {
                    this.setHasSunglasses(true);
                    this.consumeItemFromStack(player, stack);
                    return true;
                } else if(stack.getItem() instanceof IDogItem && this.canInteract(player) && !this.isIncapacicated()) {
                    IDogItem treat = (IDogItem) stack.getItem();
                    EnumActionResult treatResult = treat.onInteractWithDog(this, this.world, player, hand);

                    switch(treatResult) {
                        case SUCCESS: return true;
                        case FAIL: return false;
                        case PASS: break;
                    }

                } else if(stack.getItem() == ModItems.COLLAR_SHEARS && this.canInteract(player)) {
                    if(!this.world.isRemote) {
                        if(this.hasCollar() || this.hasSunglasses() || this.hasCape()) {
                            this.reversionTime = 40;
                            if(this.hasCollarColoured()) {
                                ItemStack collarDrop = new ItemStack(ModItems.WOOL_COLLAR, 1);
                                if(this.isCollarColoured()) {
                                    collarDrop.setTagCompound(new NBTTagCompound());
                                    collarDrop.getTagCompound().setInteger("collar_colour", this.getCollarData());
                                }
                                this.entityDropItem(collarDrop, 1);
                                this.setNoCollar();
                            }

                            if(this.hasFancyCollar()) {
                                Item drop = ModItems.MULTICOLOURED_COLLAR;
                                if(this.getCollarData() == -3)
                                    drop = ModItems.CREATIVE_COLLAR;
                                else if(this.getCollarData() == -4)
                                    drop = ModItems.SPOTTED_COLLAR;

                                this.dropItem(drop, 1);
                                this.setNoCollar();
                            }

                            if(this.hasFancyCape()) {
                                this.entityDropItem(new ItemStack(ModItems.CAPE, 1), 1);
                                this.setNoCape();
                            }

                            if(this.hasCapeColoured()) {
                                ItemStack capeDrop = new ItemStack(ModItems.CAPE_COLOURED, 1);
                                if (this.isCapeColoured()) {
                                    capeDrop.setTagCompound(new NBTTagCompound());
                                    capeDrop.getTagCompound().setInteger("cape_colour", this.getCapeData());
                                }
                                this.entityDropItem(capeDrop, 1);
                                this.setNoCape();
                            }

                            if(this.hasLeatherJacket()) {
                                this.entityDropItem(new ItemStack(ModItems.LEATHER_JACKET, 1), 1);
                                this.setNoCape();
                            }

                            if(this.hasSunglasses()) {
                                this.entityDropItem(new ItemStack(ModItems.SUNGLASSES, 1), 1);
                                this.setHasSunglasses(false);
                            }
                        } else if(this.reversionTime < 1) {
                            this.setTamed(false);
                            this.navigator.clearPath();
                            this.aiSit.setSitting(false);
                            this.setHealth(8);
                            this.TALENTS.resetTalents();
                            this.setOwnerId(null);
                            this.dataManager.set(LAST_KNOWN_NAME, Optional.absent());
                            this.setWillObeyOthers(false);
                            this.MODE.setMode(EnumMode.DOCILE);
                            if(this.hasRadarCollar())
                                this.dropItem(ModItems.RADIO_COLLAR, 1);
                            this.hasRadarCollar(false);
                            this.reversionTime = 40;
                        }
                    }

                    return true;
                } else if(stack.getItem() == Items.CAKE && this.canInteract(player) && this.isIncapacicated()) {
                    this.consumeItemFromStack(player, stack);

                    if(!this.world.isRemote) {
                        this.aiSit.setSitting(true);
                        this.setHealth(this.getMaxHealth());
                        this.setDogHunger(ConfigValues.HUNGER_POINTS);
                        this.regenerationTick = 0;
                        this.setAttackTarget((EntityLivingBase) null);
                        this.playTameEffect(true);
                        this.world.setEntityState(this, (byte) 7);
                    }

                    return true;
                } else if(stack.getItem() == Items.DYE && this.canInteract(player) && this.hasCollarColoured()) { //TODO Add Plants compatibility

                    if(!this.world.isRemote) {
                        int[] aint = new int[3];
                        int maxCompSum = 0;
                        int count = 1; //The number of different sources of colour

                        EnumDyeColor colour = EnumDyeColor.byDyeDamage(stack.getMetadata());
                        if(colour == null) {
                            return false;
                        }

                        float[] afloat = colour.getColorComponentValues();
                        int l1 = (int)(afloat[0] * 255.0F);
                        int i2 = (int)(afloat[1] * 255.0F);
                        int j2 = (int)(afloat[2] * 255.0F);
                        maxCompSum += Math.max(l1, Math.max(i2, j2));
                        aint[0] += l1;
                        aint[1] += i2;
                        aint[2] += j2;

                        if(this.isCollarColoured()) {
                            int l = this.getCollarData();
                            float f = (l >> 16 & 255) / 255.0F;
                            float f1 = (l >> 8 & 255) / 255.0F;
                            float f2 = (l & 255) / 255.0F;
                            maxCompSum = (int)(maxCompSum + Math.max(f, Math.max(f1, f2)) * 255.0F);
                            aint[0] = (int) (aint[0] + f * 255.0F);
                            aint[1] = (int) (aint[1] + f1 * 255.0F);
                            aint[2] = (int) (aint[2] + f2 * 255.0F);
                            count++;
                        }


                        int i1 = aint[0] / count;
                        int j1 = aint[1] / count;
                        int k1 = aint[2] / count;
                        float f3 = (float) maxCompSum / (float) count;
                        float f4 = Math.max(i1, Math.max(j1, k1));
                        i1 = (int)(i1 * f3 / f4);
                        j1 = (int)(j1 * f3 / f4);
                        k1 = (int)(k1 * f3 / f4);
                        int k2 = (i1 << 8) + j1;
                        k2 = (k2 << 8) + k1;
                        this.setCollarData(k2);
                    }

                    return true;
                } else if(stack.getItem() == ModItems.TREAT_BAG && this.getDogHunger() < ConfigValues.HUNGER_POINTS && this.canInteract(player) && !this.isIncapacicated()) {

                    InventoryTreatBag treatBag = new InventoryTreatBag(player.inventory.currentItem, stack);
                    treatBag.openInventory(player);

                    int slotIndex = DogUtil.getFirstSlotWithFood(this, treatBag);
                    if (slotIndex >= 0)
                        DogUtil.feedDog(this, treatBag, slotIndex);

                    treatBag.closeInventory(player);
                    return true;
                }
            }

            if(!this.isBreedingItem(stack) && this.canInteract(player)) {
                if(!this.world.isRemote) {
                    this.aiSit.setSitting(!this.isSitting());
                    this.isJumping = false;
                    this.navigator.clearPath();
                    this.setAttackTarget((EntityLivingBase) null);
                }
                return true;
            }
        } else if(stack.getItem() == ModItems.COLLAR_SHEARS && this.reversionTime < 1) {
            if(!this.world.isRemote) {
                this.locationManager.remove(this);
                this.setDead();
                EntityWolf wolf = new EntityWolf(this.world);
                wolf.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
                wolf.setHealth(this.getHealth());
                wolf.setGrowingAge(this.getGrowingAge());
                this.world.spawnEntity(wolf);
            }
            return true;
        } else if(stack.getItem() == Items.BONE || stack.getItem() == ModItems.TRAINING_TREAT) {
            this.consumeItemFromStack(player, stack);

            if(!this.world.isRemote) {
                if(stack.getItem() == ModItems.TRAINING_TREAT || this.rand.nextInt(3) == 0) {
                    this.setTamed(true);
                    this.navigator.clearPath();
                    this.setAttackTarget((EntityLivingBase) null);
                    this.aiSit.setSitting(true);
                    this.setHealth(20.0F);
                    this.setOwnerId(player.getUniqueID());
                    this.playTameEffect(true);
                    this.world.setEntityState(this, (byte) 7);
                } else {
                    this.playTameEffect(false);
                    this.world.setEntityState(this, (byte) 6);
                }
            }

            return true;
        }

        return super.processInteract(player, hand);
    }

    @Override
    public EntityDog createChild(EntityAgeable entityAgeable) {
        EntityDog entitydog = new EntityDog(this.world);
        UUID uuid = this.getOwnerId();

        if (uuid != null) {
            entitydog.setOwnerId(uuid);
            entitydog.setTamed(true);
        }

        entitydog.setGrowingAge(-ConfigValues.TIME_TO_MATURE);

        if(ConfigValues.PUPS_GET_PARENT_LEVELS && entityAgeable instanceof EntityDog) {
            int combinedLevel = this.LEVELS.getLevel() + ((EntityDog)entityAgeable).LEVELS.getLevel();
            combinedLevel /= 2;
            combinedLevel = Math.min(combinedLevel, 20);
            entitydog.LEVELS.setLevel(combinedLevel);
        }

        return entitydog;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return DoggyTalentsAPI.BREED_WHITELIST.containsItem(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean getAlwaysRenderNameTagForRender() {
        return this.hasCustomName();
    }

    @Override
    protected boolean isMovementBlocked() {
        return this.isPlayerSleeping() || super.isMovementBlocked(); //this.getRidingEntity() != null || this.riddenByEntity instanceof EntityPlayer || super.isMovementBlocked();
    }

    @Override
    public boolean isPotionApplicable(PotionEffect potionEffect) {
        switch (TalentHelper.isPostionApplicable(this, potionEffect)) {
            case SUCCESS: return true;
            case FAIL: return false;
            case PASS: break;
        }

        return !this.isIncapacicated();
    }

    @Override
    public void setFire(int amount) {
        if (TalentHelper.setFire(this, amount))
            super.setFire(amount);
    }

    @Override
    public boolean isPlayerSleeping() {
        return false;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return TalentHelper.shouldDecreaseAir(this, air) ? super.decreaseAirSupply(air) : air;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return TalentHelper.canBreatheUnderwater(this);
    }

    @Override
    protected boolean canTriggerWalking() {
        return TalentHelper.canTriggerWalking(this);
    }

    @Override
    public boolean shouldAttackEntity(EntityLivingBase target, EntityLivingBase owner) {
        if (TalentHelper.canAttackEntity(this, target))
            return true;

        if (!(target instanceof EntityCreeper) && !(target instanceof EntityGhast)) {
            if (target instanceof EntityDog) {
                EntityDog entitydog = (EntityDog) target;

                if (entitydog.isTamed() && entitydog.getOwner() == owner)
                    return false;
            } else if (target instanceof EntityWolf) {
                EntityWolf entitywolf = (EntityWolf) target;

                if (entitywolf.isTamed() && entitywolf.getOwner() == owner)
                    return false;
            }

            if (target instanceof EntityPlayer && owner instanceof EntityPlayer && !((EntityPlayer) owner).canAttackPlayer((EntityPlayer) target))
                return false;
            else if (target == owner)
                return false;
            else
                return !(target instanceof AbstractHorse) || !((AbstractHorse) target).isTame();
        }

        return false;
    }

    @Override
    public boolean canAttackClass(Class<? extends EntityLivingBase> cls) {
        if (TalentHelper.canAttackClass(this, cls))
            return true;

        return super.canAttackClass(cls);
    }

    @Override
    public Entity changeDimension(int dimId, ITeleporter teleporter) {
        Entity entity = super.changeDimension(dimId, teleporter);
        if(entity instanceof EntityDog) {
            EntityDog dog = (EntityDog)entity;

            if(!this.world.isRemote) {
                dog.locationManager.update(dog);
                this.locationManager.remove(this);
            }
        } else if(entity != null) {
            DoggyTalents.LOGGER.warn("Dog tried to change dimension but now isn't a dog?");
        }

        return entity;
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if(!this.world.isRemote)
            this.locationManager.update(this);
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if(!this.world.isRemote && !this.isEntityAlive())
            this.locationManager.remove(this);
    }

    @Override
    public void setDead() {
        super.setDead();
        if(!this.world.isRemote)
            this.locationManager.remove(this);
    }

    @Override
    public void onDeath(DamageSource cause) {
        if(!this.isImmortal()) {
            this.isWet = false;
            this.isShaking = false;
            this.prevTimeDogIsShaking = 0.0F;
            this.timeDogIsShaking = 0.0F;

            if(!this.world.isRemote) {
                this.locationManager.remove(this);

                if(this.world.getGameRules().getBoolean("showDeathMessages") && this.getOwner() instanceof EntityPlayerMP) {
                    this.getOwner().sendMessage(this.getCombatTracker().getDeathMessage());
                }
            }
        }
    }

    @Override
    protected float getJumpUpwardsMotion() {
        return 0.42F;
    }

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.8F;
    }

    @Override
       public boolean canMateWith(EntityAnimal otherAnimal) {
           if(otherAnimal == this) {
               return false;
           } else if(!this.isTamed()) {
               return false;
           } else if(!(otherAnimal instanceof EntityDog)) {
               return false;
           } else {
               EntityDog entitydog = (EntityDog)otherAnimal;
               if(!entitydog.isTamed()) {
                   return false;
               } else if(entitydog.isSitting()) {
                   return false;
               } else if(!this.GENDER.canMateWith(entitydog)) {
                   return false;
               } else {
                   return this.isInLove() && entitydog.isInLove();
               }
           }
       }

    @Override
    public ItemStack getPickedResult(RayTraceResult target) {
        return new ItemStack(ModItems.DOGGY_CHARM);
    }

    @Override
    public void onKillEntity(EntityLivingBase entityLivingIn) {
        super.onKillEntity(entityLivingIn);
        this.STATS.incrementKillCount(entityLivingIn);
    }

    @SideOnly(Side.CLIENT)
    public boolean isDogWet() {
        return this.isWet;
    }

    @SideOnly(Side.CLIENT)
    public float getShadingWhileWet(float partialTick) {
        return 0.75F + (this.prevTimeDogIsShaking + (this.timeDogIsShaking - this.prevTimeDogIsShaking) * partialTick) / 2.0F * 0.25F;
    }

    @SideOnly(Side.CLIENT)
    public float getShakeAngle(float partialTick, float offset) {
        float f = (this.prevTimeDogIsShaking + (this.timeDogIsShaking - this.prevTimeDogIsShaking) * partialTick + offset) / 1.8F;
        if(f < 0.0F) {
            f = 0.0F;
        } else if (f > 1.0F) {
            f = 1.0F;
        }

        return MathHelper.sin(f * (float)Math.PI) * MathHelper.sin(f * (float)Math.PI * 11.0F) * 0.15F * (float)Math.PI;
    }

    @SideOnly(Side.CLIENT)
    public float getInterestedAngle(float partialTick) {
        return (this.headRotationCourseOld + (this.headRotationCourse - this.headRotationCourseOld) * partialTick) * 0.15F * (float)Math.PI;
    }

    public float getWagAngle(float partialTick, float offset) {
        float f = (this.prevTimeWolfIsHappy + (this.timeWolfIsHappy - this.prevTimeWolfIsHappy) * partialTick + offset) / 2.0F;
        if (f < 0.0F) f = 0.0F;
        else if (f > 2.0F) f %= 2.0F;
        return MathHelper.sin(f * (float) Math.PI * 11.0F) * 0.3F * (float) Math.PI;
    }

    @SideOnly(Side.CLIENT)
    public float getTailRotation() {
        return this.isTamed() ? (0.55F - (this.getMaxHealth() - this.dataManager.get(DATA_HEALTH_ID)) / this.getMaxHealth() * 20.0F * 0.02F) * (float)Math.PI : ((float)Math.PI / 5F);
    }

   @Override
   public float getEyeHeight() {
       return this.height * 0.8F;
   }

   @Override
   public int getVerticalFaceSpeed() {
       return this.isSitting() ? 20 : super.getVerticalFaceSpeed();
   }

    public boolean isImmortal() {
        return this.isTamed() && ConfigValues.DOGS_IMMORTAL || this.LEVELS.isDireDog();
    }

    public boolean isIncapacicated() {
        return this.isImmortal() && this.getHealth() <= ConfigValues.LOW_HEATH_LEVEL;
    }

    public double effectiveLevel() {
        return (this.LEVELS.getLevel() + this.LEVELS.getDireLevel()) / 10.0D;
    }

    public double getHealthRelative() {
        return getHealth() / (double) getMaxHealth();
    }

    public boolean canWander() {
        return this.isTamed() && this.MODE.isMode(EnumMode.WANDERING) && this.COORDS.hasBowlPos() && this.getDistanceSq(this.COORDS.getBowlPos()) < 400.0D;
    }

    @Override
    public boolean canInteract(EntityLivingBase player) {
        return this.isOwner(player) || this.willObeyOthers();
    }

    public int foodValue(ItemStack stack) {
        if (stack.isEmpty())
            return 0;

        int foodValue = 0;

        Item item = stack.getItem();

        if (stack.getItem() != Items.ROTTEN_FLESH && item instanceof ItemFood) {
            ItemFood itemfood = (ItemFood)item;

            if (itemfood.isWolfsFavoriteMeat())
                foodValue = 40;
        } else if (stack.getItem() == ModItems.CHEW_STICK) {
            return 10;
        }

        foodValue = TalentHelper.changeFoodValue(this, stack, foodValue);

        return foodValue;
    }

    public int nourishment() {
        int amount = 0;

        if (this.getDogHunger() > 0) {
            amount = 40 + 4 * (MathHelper.floor(this.effectiveLevel()) + 1);

            if (isSitting() && this.TALENTS.getLevel(ModTalents.QUICK_HEALER) == 5) {
                amount += 20 + 2 * (MathHelper.floor(this.effectiveLevel()) + 1);
            }

            if (!this.isSitting()) {
                amount *= 5 + this.TALENTS.getLevel(ModTalents.QUICK_HEALER);
                amount /= 10;
            }
        }

        return amount;
    }

    public void mountTo(EntityLivingBase entityLiving) {
        entityLiving.rotationYaw = this.rotationYaw;
        entityLiving.rotationPitch = this.rotationPitch;

        if(!this.world.isRemote)
            entityLiving.startRiding(this);
    }

    public int points() {
        return this.isCreativeCollar() ? 1000 : this.LEVELS.getLevel() + this.LEVELS.getDireLevel() + (this.LEVELS.isDireDog() ? 15 : 0) + (this.getGrowingAge() < 0 ? 0 : 15);
    }

    public int spendablePoints() {
        return this.points() - this.usedPoints();
    }

    public int usedPoints() {
        return TalentHelper.getUsedPoints(this);
    }

    public int deductive(int id) {
        if(id >= 1 && id <= 5)
            return new int[] {1,3,6,10,15}[id - 1];

        return 0;
    }

    public ITextComponent getOwnersName() {
        if(this.getOwner() != null) {
            return this.getOwner().getDisplayName();
        } else if(this.dataManager.get(LAST_KNOWN_NAME).isPresent()) {
            return this.dataManager.get(LAST_KNOWN_NAME).get();
        } else if(this.getOwnerId() != null) {
            return new TextComponentTranslation("entity.doggytalents.dog.unknown_owner");
        } else {
            return new TextComponentTranslation("entity.doggytalents.dog.untamed");
        }
    }

    private boolean getDogFlag(int bit) {
        return (this.dataManager.get(DOG_FLAGS) & bit) != 0;
    }

    private void setDogFlag(int bit, boolean flag) {
        byte b0 = this.dataManager.get(DOG_FLAGS);
        if(flag) {
            this.dataManager.set(DOG_FLAGS, (byte)(b0 | bit));
        } else {
            this.dataManager.set(DOG_FLAGS, (byte)(b0 & ~bit));
        }
    }

    public void setBegging(boolean flag) {
        this.setDogFlag(1, flag);
    }

    public boolean isBegging() {
        return this.getDogFlag(1);
    }

    public void setWillObeyOthers(boolean flag) {
        this.setDogFlag(2, flag);
    }

    public boolean willObeyOthers() {
        return this.getDogFlag(2);
    }

    public void setCanPlayersAttack(boolean flag) {
        this.setDogFlag(4, flag);
    }

    public boolean canPlayersAttack() {
        return this.getDogFlag(4);
    }

    public void hasRadarCollar(boolean flag) {
        this.setDogFlag(8, flag);
    }

    public boolean hasRadarCollar() {
        return this.getDogFlag(8);
    }

    public void setHasSunglasses(boolean hasSunglasses) {
        this.setDogFlag(16, hasSunglasses);
    }

    public boolean hasSunglasses() {
        return this.getDogFlag(16);
    }

    public void setLyingDown(boolean lying) {
        this.setDogFlag(32, lying);
    }

    public boolean isLyingDown() {
        return this.getDogFlag(32);
    }

    public int getTameSkin() {
        return this.dataManager.get(DOG_TEXTURE);
    }

    public void setTameSkin(int index) {
        this.dataManager.set(DOG_TEXTURE, (byte)index);
    }

    public int getDogHunger() {
        return this.dataManager.get(HUNGER_PARAM).intValue();
    }

    public void setDogHunger(int par1) {
        this.dataManager.set(HUNGER_PARAM, Math.min(ConfigValues.HUNGER_POINTS, Math.max(0, par1)));
    }

    public void setBoneVariant(ItemStack stack) {
        this.dataManager.set(BONE_VARIANT, stack);
    }

    public ItemStack getBoneVariant() {
        return this.dataManager.get(BONE_VARIANT);
    }

    @Nullable
    public IThrowableItem getThrowableItem() {
        Item item = this.dataManager.get(BONE_VARIANT).getItem();
        if(item instanceof IThrowableItem) {
            return (IThrowableItem)item;
        } else {
            return null;
        }
    }

    public boolean hasBone() {
        return !this.getBoneVariant().isEmpty();
    }

    public int getCollarData() {
        return this.dataManager.get(COLLAR_COLOUR);
    }

    public void setCollarData(int value) {
        this.dataManager.set(COLLAR_COLOUR, value);
    }

    public int getCapeData() {
        return this.dataManager.get(CAPE);
    }

    public void setCapeData(int value) {
        this.dataManager.set(CAPE, value);
    }

    @Override
    public void setDogSize(int value) {
        this.dataManager.set(SIZE, (byte)Math.min(5, Math.max(1, value)));
    }

    @Override
    public int getDogSize() {
        return this.dataManager.get(SIZE);
    }

    public void setGender(EnumGender gender) {
        this.dataManager.set(GENDER_PARAM, (byte)gender.getIndex());
    }

    public EnumGender getGender() {
        return EnumGender.byIndex(this.dataManager.get(GENDER_PARAM));
    }

    public void setLevel(int level) {
        this.dataManager.set(LEVEL,  (byte)level);
    }

    public int getLevel() {
        return this.dataManager.get(LEVEL);
    }

    public void setDireLevel(int level) {
        this.dataManager.set(LEVEL_DIRE, (byte)level);
    }

    public int getDireLevel() {
        return this.dataManager.get(LEVEL_DIRE);
    }

    public void setMode(EnumMode mode) {
        EnumMode prevMode = this.getMode();
        this.dataManager.set(MODE_PARAM, (byte)mode.getIndex());
        mode.onModeSet(this, prevMode);
    }

    public EnumMode getMode() {
        return EnumMode.byIndex(this.dataManager.get(MODE_PARAM));
    }

    public void setTalentMap(Map<Talent, Integer> data) {
        this.dataManager.set(TALENTS_PARAM, data);
    }

    public Map<Talent, Integer> getTalentMap() {
        return this.dataManager.get(TALENTS_PARAM);
    }

    public boolean hasBedPos() {
        return this.dataManager.get(BED_POS).isPresent();
    }

    public boolean hasBowlPos() {
        return this.dataManager.get(BOWL_POS).isPresent();
    }

    public BlockPos getBedPos() {
        return this.dataManager.get(BED_POS).or(this.world.getSpawnPoint());
    }

    public BlockPos getBowlPos() {
        return this.dataManager.get(BOWL_POS).or(this.getPosition());
    }

    public void resetBedPosition() {
        this.dataManager.set(BED_POS, Optional.absent());
    }

    public void resetBowlPosition() {
        this.dataManager.set(BOWL_POS, Optional.absent());
    }

    public void setBedPos(BlockPos pos) {
        this.dataManager.set(BED_POS, Optional.of(pos));
    }

    public void setBowlPos(BlockPos pos) {
        this.dataManager.set(BOWL_POS, Optional.of(pos));
    }


    public void setNoCollar() {
        this.setCollarData(-2);
    }

    public boolean hasCollar() {
        return this.getCollarData() != -2;
    }

    public boolean hasCollarColoured() {
        return this.getCollarData() >= -1;
    }

    public boolean isCollarColoured() {
        return this.getCollarData() > -1;
    }

    public void setHasCollar() {
        this.setCollarData(-1);
    }

    public boolean hasFancyCollar() {
        return this.getCollarData() < -2;
    }

    public int getFancyCollarIndex() {
        return -3 - this.getCollarData();
    }

    public boolean isCreativeCollar() {
        return this.getCollarData() == -3;
    }

    public float[] getCollar() {
        return DogUtil.rgbIntToFloatArray(this.getCollarData());
    }

    public boolean hasCape() {
        return this.getCapeData() != -2;
    }

    public boolean hasCapeColoured() {
        return this.getCapeData() >= -1;
    }

    public boolean hasFancyCape() {
        return this.getCapeData() == -3;
    }

    public boolean hasLeatherJacket() {
        return this.getCapeData() == -4;
    }

    public boolean isCapeColoured() {
        return this.getCapeData() > -1;
    }

    public void setFancyCape() {
        this.setCapeData(-3);
    }

    public void setLeatherJacket() {
        this.setCapeData(-4);
    }

    public void setCapeColoured() {
        this.setCapeData(-1);
    }

    public void setNoCape() {
        this.setCapeData(-2);
    }

    public float[] getCapeColour() {
        return DogUtil.rgbIntToFloatArray(this.getCapeData());
    }

    protected boolean dogJumping;
    protected float jumpPower;

    public boolean isDogJumping() {
        return this.dogJumping;
    }

    public void setDogJumping(boolean jumping) {
        this.dogJumping = jumping;
    }

    public double getDogJumpStrength() {
        float verticalVelocity = 0.42F + 0.06F * this.TALENTS.getLevel(ModTalents.WOLF_MOUNT);
        if(this.TALENTS.getLevel(ModTalents.WOLF_MOUNT) == 5) verticalVelocity += 0.04F;
        return verticalVelocity;
    }

    @Override
    public Entity getControllingPassenger() {
        return this.getPassengers().isEmpty() ? null : (Entity) this.getPassengers().get(0);
    }

    @Override
    public boolean canBeSteered() {
        return this.getControllingPassenger() instanceof EntityLivingBase;
    }

    @Override
    public boolean canBePushed() {
        return !this.isBeingRidden();
    }

    @Override
    public void updatePassenger(Entity passenger) {
        super.updatePassenger(passenger);
        if(passenger instanceof EntityLiving) {
            EntityLiving entityliving = (EntityLiving)passenger;
            this.renderYawOffset = entityliving.renderYawOffset;
        }
    }

    @Override
    public double getYOffset() {
        return this.getRidingEntity() instanceof EntityPlayer ? 0.5D : 0.0D;
    }

    @Override
    public boolean shouldDismountInWater(Entity rider) {
        switch (TalentHelper.canBeRiddenInWater(this, rider)) {
        case SUCCESS: return false;
        case FAIL: return true;
        default: return true;
    }
    }

    @Override
    public boolean canRiderInteract() {
        return true;
    }

    // 0 - 100 input
    public void setJumpPower(int jumpPowerIn) {
        if(this.TALENTS.getLevel(ModTalents.WOLF_MOUNT) > 0) {
            this.jumpPower = 1.0F;
        }
    }

    public boolean canJump() {
        return this.TALENTS.getLevel(ModTalents.WOLF_MOUNT) > 0;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if(this.isBeingRidden() && this.canBeSteered() && this.TALENTS.getLevel(ModTalents.WOLF_MOUNT) > 0) {
            EntityLivingBase entitylivingbase = (EntityLivingBase)this.getControllingPassenger();
            this.rotationYaw = entitylivingbase.rotationYaw;
            this.prevRotationYaw = this.rotationYaw;
            this.rotationPitch = entitylivingbase.rotationPitch * 0.5F;
            this.setRotation(this.rotationYaw, this.rotationPitch);
            this.renderYawOffset = this.rotationYaw;
            this.rotationYawHead = this.renderYawOffset;
            strafe = entitylivingbase.moveStrafing * 0.7F;
            forward = entitylivingbase.moveForward;
            if (forward <= 0.0F) {
                forward *= 0.5F;
            }

            this.stepHeight = 1.0F;

            if(this.jumpPower > 0.0F && !this.isDogJumping() && this.onGround) {
                this.motionY = this.getDogJumpStrength() * this.jumpPower;
                if(this.isPotionActive(MobEffects.JUMP_BOOST)) {
                    this.motionY += (this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1F;
                }

                this.setDogJumping(true);
                this.isAirBorne = true;
                if(forward > 0.0F) {
                    float f = MathHelper.sin(this.rotationYaw * ((float)Math.PI / 180F));
                    float f1 = MathHelper.cos(this.rotationYaw * ((float)Math.PI / 180F));
                    this.motionX += -0.4F * f * this.jumpPower;
                    this.motionZ += 0.4F * f1 * this.jumpPower;
                }

                this.jumpPower = 0.0F;
            }
            else if(this.jumpPower > 0.0F && this.isInWater() && !this.isDogJumping()) {
                this.motionY = this.getDogJumpStrength() * 0.4F;
                this.setDogJumping(true);
                  this.jumpPower = 0.0F;
            }

            this.jumpMovementFactor = this.getAIMoveSpeed() * 0.3F;
            if(this.canPassengerSteer()) {
                this.setAIMoveSpeed((float)this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).getAttributeValue() * 0.5F);
                super.travel(strafe, vertical, forward);
            } else if(entitylivingbase instanceof EntityPlayer) {
                this.motionX = 0.0D;
                this.motionY = 0.0D;
                this.motionZ = 0.0D;
            }

            if(this.onGround || this.isInWater()) {
                this.jumpPower = 0.0F;
                this.setDogJumping(false);
            }

            this.prevLimbSwingAmount = this.limbSwingAmount;
            double d1 = this.posX - this.prevPosX;
            double d0 = this.posZ - this.prevPosZ;
            float f2 = MathHelper.sqrt(d1 * d1 + d0 * d0) * 4.0F;
            if(f2 > 1.0F) {
                f2 = 1.0F;
            }

            this.limbSwingAmount += (f2 - this.limbSwingAmount) * 0.4F;
            this.limbSwing += this.limbSwingAmount;
        } else {
            this.stepHeight = 0.6F;
            this.jumpMovementFactor = 0.02F;
            super.travel(strafe, vertical, forward);
        }
    }

    @Override
    public ICoordFeature getCoordFeature() {
        return this.COORDS;
    }

    @Override
    public IGenderFeature getGenderFeature() {
        return this.GENDER;
    }

    @Override
    public ILevelFeature getLevelFeature() {
        return this.LEVELS;
    }

    @Override
    public IModeFeature getModeFeature() {
        return this.MODE;
    }

    @Override
    public IStatsFeature getStatsFeature() {
        return this.STATS;
    }

    @Override
    public ITalentFeature getTalentFeature() {
        return this.TALENTS;
    }

    @Override
    public IHungerFeature getHungerFeature() {
        return this.HUNGER;
    }

    @Override
    public <T> void putObject(String key, T i) {
        this.objects.put(key, i);
    }

    @Override
    public <T> T getObject(String key, Class<T> type) {
        return (T) this.objects.get(key);
    }
}