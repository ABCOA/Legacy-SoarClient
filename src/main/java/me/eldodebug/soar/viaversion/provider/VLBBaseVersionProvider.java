package me.eldodebug.soar.viaversion.provider;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.protocols.base.BaseVersionProvider;

import me.eldodebug.soar.viaversion.ViaLoadingBase;

public class VLBBaseVersionProvider extends BaseVersionProvider {

    @Override
    public int getClosestServerProtocol(UserConnection connection) throws Exception {
        if (connection.isClientSide()) {
            return ViaLoadingBase.getInstance().getTargetVersion().getVersion();
        }
        return super.getClosestServerProtocol(connection);
    }
}
