package com.johnymuffin.jvillage.beta.models;

import com.johnymuffin.jvillage.beta.JVillage;
import com.johnymuffin.jvillage.beta.interfaces.ClaimManager;
import com.johnymuffin.jvillage.beta.models.chunk.VChunk;
import com.johnymuffin.jvillage.beta.models.chunk.VClaim;

public class District implements ClaimManager {

    private final JVillage plugin;
    private boolean modified = false;

    public District(JVillage plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean addClaim(VClaim vChunk) {
        modified = true;
        return plugin.getDistrictClaimsArray(this).add(vChunk);
    }

    @Override
    public boolean removeClaim(VClaim vChunk) {
        modified = true;
        return plugin.getDistrictClaimsArray(this).remove(vChunk);
    }

    @Override
    public boolean isClaimed(VChunk vChunk) {
        return plugin.getDistrictClaimsArray(this).contains(vChunk);
    }
}
