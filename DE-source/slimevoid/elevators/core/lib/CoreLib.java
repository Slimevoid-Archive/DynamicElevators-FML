package slimevoid.elevators.core.lib;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CoreLib {

	public static final String	MOD_ID				= "DynamicElevators";
	public static final String	MOD_RESOURCES		= "elevators";
	public static final String	MOD_NAME			= "Dynamic Elevators";
	public static final String	MOD_VERSION			= "2.0.0.7";
	public static final String	MOD_DEPENDENCIES	= "required-after:SlimevoidLib";
	public static final String	MOD_CHANNEL			= "DELEVATORS";
	public static final String	CLIENT_PROXY		= "slimevoid.collaborative.client.proxy.ClientProxy";
	public static final String	COMMON_PROXY		= "slimevoid.collaborative.proxy.CommonProxy";
	@SideOnly(Side.CLIENT)
	public static boolean		OPTIFINE_INSTALLED	= FMLClientHandler.instance().hasOptifine();

}
