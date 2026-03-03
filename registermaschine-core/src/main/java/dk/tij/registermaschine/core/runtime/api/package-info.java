/**
 * Defines the core interfaces for the Registermaschine's execution environment.
 * <p>
 *     This package provides the abstraction of the hardware state and the observe
 *     mechanism for state changes. Key components include:
 * </p>
 * <ul>
 *     <li>{@link dk.tij.registermaschine.core.runtime.api.IExecutionContext}:
 *     Represents the machines registers, accumulator, Programme Counter (PC), and halt state.
 *     It is the primary "Receiver" for all instruction logic.</li>
 *     <li>{@link dk.tij.registermaschine.core.runtime.api.IExecutionContextListener}:
 *     An observer interface that allows external components (like Debuggers or GUIs)
 *     to react to state changes, such as register updates or instruction steps.</li>
 * </ul>
 *
 * @author TiJ
 */
package dk.tij.registermaschine.core.runtime.api;