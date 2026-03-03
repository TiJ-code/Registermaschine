/**
 * Defines the contract for configuration parsers within the Registermaschine.
 * <p>
 *     This package contains the {@link dk.tij.registermaschine.core.config.api.IConfigParser} interface.
 *     It allows the configuration system to be extended with custom parsing strategies for different
 *     segments of the machine's setup, such as instruction sets, hardware limits, or memory layouts.
 * </p>
 * <h3>Extensions:</h3>
 * Parsers implementing this API are typically registered within the
 * {@link dk.tij.registermaschine.core.config.CoreConfigParser} to be executed during the
 * initialisation phase of the application.
 *
 * @author TiJ
 */
package dk.tij.registermaschine.core.config.api;