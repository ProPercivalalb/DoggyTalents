package doggytalents.proxy;
import java.util.Random;

import doggytalents.ModItems;
import doggytalents.base.ObjectLibClient;
import doggytalents.client.gui.GuiDogInfo;
import doggytalents.client.model.block.IStateParticleModel;
import doggytalents.client.renderer.entity.ParticleCustomLanding;
import doggytalents.client.renderer.entity.RenderDog;
import doggytalents.client.renderer.entity.RenderDogBeam;
import doggytalents.entity.EntityDog;
import doggytalents.entity.EntityDoggyBeam;
import doggytalents.handler.GameOverlay;
import doggytalents.handler.KeyState;
import doggytalents.talent.WorldRender;
import doggytalents.tileentity.TileEntityFoodBowl;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author ProPercivalalb
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

	@Override
	public void preInit(FMLPreInitializationEvent event) {
		super.preInit(event);
		ObjectLibClient.INITIALIZATION.preInit(event);
		
		ClientRegistry.registerKeyBinding(KeyState.come);
		ClientRegistry.registerKeyBinding(KeyState.stay);
		ClientRegistry.registerKeyBinding(KeyState.ok);
		ClientRegistry.registerKeyBinding(KeyState.heel);
		
		RenderingRegistry.registerEntityRenderingHandler(EntityDog.class, RenderDog::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityDoggyBeam.class, RenderDogBeam::new);
	}
	
	@Override
	public void init(FMLInitializationEvent event) {
		super.init(event);
		ObjectLibClient.INITIALIZATION.init(event);
	}
	
	@Override
	public void postInit(FMLPostInitializationEvent event) {
		super.postInit(event);
		ObjectLibClient.INITIALIZATION.postInit(event);
		
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {

			@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if(stack.hasTagCompound())
					if(stack.getTagCompound().hasKey("collar_colour"))
						return stack.getTagCompound().getInteger("collar_colour");
				return -1;
			}
			
		}, ModItems.WOOL_COLLAR);
		
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {

		@Override
			public int getColorFromItemstack(ItemStack stack, int tintIndex) {
				if(stack.hasTagCompound())
					if(stack.getTagCompound().hasKey("cape_colour"))
						return stack.getTagCompound().getInteger("cape_colour");
				return -1;
			}
			
		}, ModItems.CAPE_COLOURED);
	}
	
	@Override
    protected void registerEventHandlers() {
        super.registerEventHandlers();
        MinecraftForge.EVENT_BUS.register(new WorldRender());
		MinecraftForge.EVENT_BUS.register(new GameOverlay());
		MinecraftForge.EVENT_BUS.register(new KeyState());
    }
	

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) { 
		if(ID == GUI_ID_DOGGY) {
			Entity target = player.world.getEntityByID(x);
            if(!(target instanceof EntityDog))
            	return null;
			EntityDog dog = (EntityDog)target;
			return new GuiDogInfo(dog, player);
		}
		else if(ID == GUI_ID_PACKPUPPY) {
			Entity target = player.world.getEntityByID(x);
            if(!(target instanceof EntityDog)) 
            	return null;
			EntityDog dog = (EntityDog)target;
			return ObjectLibClient.createGuiPackPuppy(player, dog);
		}
		else if(ID == GUI_ID_FOOD_BOWL) {
			TileEntity target = world.getTileEntity(new BlockPos(x, y, z));
			if(!(target instanceof TileEntityFoodBowl))
				return null;
			TileEntityFoodBowl foodBowl = (TileEntityFoodBowl)target;
			return ObjectLibClient.createGuiFoodBowl(player.inventory, foodBowl);
		}
		else if(ID == GUI_ID_FOOD_BAG) {
			return ObjectLibClient.createGuiTreatBag(player, x, player.inventory.getStackInSlot(x));
		}
		return null;
	}
	
	@Override
	public EntityPlayer getPlayerEntity(MessageContext ctx) {
		return (ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx));
	}
	
	@Override
	public EntityPlayer getPlayerEntity() {
		return Minecraft.getMinecraft().player;
	}
	
	@Override
	public IThreadListener getThreadFromContext(MessageContext ctx) {
		return (ctx.side.isClient() ? Minecraft.getMinecraft() : super.getThreadFromContext(ctx));
	}
	
	@Override
	public void spawnCrit(World world, Entity entity) {
		FMLClientHandler.instance().getClient().effectRenderer.emitParticleAtEntity(entity, EnumParticleTypes.CRIT);
	}
	
	@Override
	public void spawnCustomParticle(EntityPlayer player, BlockPos pos, Random rand, float posX, float posY, float posZ, int numberOfParticles, float particleSpeed) {
		TextureAtlasSprite sprite;

		IBlockState state = player.world.getBlockState(pos);
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		if(model instanceof IStateParticleModel) {
			state = state.getBlock().getExtendedState(state.getActualState(player.world, pos), player.world, pos);
			sprite = ((IStateParticleModel)model).getParticleTexture(state);
		} 
		else
			sprite = model.getParticleTexture();
		
		ParticleManager manager = Minecraft.getMinecraft().effectRenderer;

		for(int i = 0; i < numberOfParticles; i++) {
			double xSpeed = rand.nextGaussian() * particleSpeed;
			double ySpeed = rand.nextGaussian() * particleSpeed;
			double zSpeed = rand.nextGaussian() * particleSpeed;
			
			Particle particle = new ParticleCustomLanding(player.world, posX, posY, posZ, xSpeed, ySpeed, zSpeed, state, pos, sprite);
			manager.addEffect(particle);
		}
	}
}
