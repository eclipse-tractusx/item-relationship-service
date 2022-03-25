package net.catenax.irs.component.enums;

/**
 * Represents the state of the current job
 */
public enum JobState {
   UNSAVED,
   INITIAL,
   IN_PROGRESS,
   TRANSFERS_FINISHED,
   COMPLETED,
   ERROR;
}
