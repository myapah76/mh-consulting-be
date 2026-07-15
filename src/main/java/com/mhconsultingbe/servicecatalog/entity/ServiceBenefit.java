package com.mhconsultingbe.servicecatalog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_benefits")
public class ServiceBenefit extends ServiceListItem {}
