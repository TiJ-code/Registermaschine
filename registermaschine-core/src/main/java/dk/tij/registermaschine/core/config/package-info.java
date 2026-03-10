/**
 * Provides the central configuration and initialisation logic for the Registermaschine.
 * <p>
 *     This packages serves as the "Source of Truth" for the system. It contains:
 * </p>
 * <ul>
 *     <li>{@link dk.tij.registermaschine.core.config.CoreConfig}: A global repository for
 *     runtime settings, instruction registries, and hardware constraints.</li>
 *     <li>{@link dk.tij.registermaschine.core.config.CoreConfigParser}: The orchestrator
 *     responsible for invoking internal and external parsers to populate the system configuration</li>
 * </ul>
 * <b>Initilisation Workflow</b>
 * The configuration is usually loaded from XML files (e.g. {@code default.instructions.jxml}).
 * The {@code CoreConfigParser} manages the high-level XML traversal and delegates
 * tags to the internal parsing sub-system
 * @see dk.tij.registermaschine.core.config.api.IConfigParser
 *
 * @since 1.0.0
 * @author TiJ
 */
package dk.tij.registermaschine.core.config;