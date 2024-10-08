package com.gmoon.springquartzcluster.quartz.job_store;

import com.gmoon.springquartzcluster.quartz.job_store.constants.QuartzColumnLength;
import com.gmoon.springquartzcluster.quartz.job_store.id.QuartzTriggerId;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "QUARTZ_CRON_TRIGGERS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class QuartzSimpropTriggers {
	@EmbeddedId
	@EqualsAndHashCode.Include
	private QuartzTriggerId id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumns({
		 @JoinColumn(name = "SCHED_NAME", referencedColumnName = "SCHED_NAME", insertable = false, updatable = false),
		 @JoinColumn(name = "TRIGGER_NAME", referencedColumnName = "TRIGGER_NAME", insertable = false, updatable = false),
		 @JoinColumn(name = "TRIGGER_GROUP", referencedColumnName = "TRIGGER_GROUP", insertable = false, updatable = false)
	})
	private QuartzTriggers triggers;

	@Column(name = "STR_PROP_1", length = QuartzColumnLength.STR_PROP_1)
	private String strProp1;

	@Column(name = "STR_PROP_2", length = QuartzColumnLength.STR_PROP_2)
	private String strProp2;

	@Column(name = "STR_PROP_3", length = QuartzColumnLength.STR_PROP_3)
	private String strProp3;

	@Column(name = "INT_PROP_1")
	private Integer intProp1;

	@Column(name = "INT_PROP_2")
	private Integer intProp2;

	@Column(name = "LONG_PROP_1")
	private Long longProp1;

	@Column(name = "LONG_PROP_2")
	private Long longProp2;

	@Column(name = "DEC_PROP_1")
	private Long decProp1;

	@Column(name = "DEC_PROP_2")
	private Long decProp2;

	@Column(name = "BOOL_PROP_1", length = QuartzColumnLength.BOOL_PROP_1)
	private String boolProp1;

	@Column(name = "BOOL_PROP_2", length = QuartzColumnLength.BOOL_PROP_2)
	private String boolProp2;
}
