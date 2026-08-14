package com.rob.houserental.model;

import androidx.room.Embedded;
import androidx.room.Relation;

public class TenancyWithDetails {

    @Embedded
    public Tenancy tenancy;

    @Relation(
            parentColumn = "tenantId",
            entityColumn = "id"
    )
    public Tenant tenant;

    @Relation(
            parentColumn = "unitId",
            entityColumn = "id"
    )
    public Unit unit;
}
