package com.mhconsultingbe.servicecatalog.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "service_process_steps")
public class ServiceProcessStep extends ServiceListItem {
}
