/**
 * Provides logical composition of conditions using Boolean algebra.
 * <p>
 *     This package contains composite conditions such as {@code And}, {@code Or},
 *     and {@code Not}. These allow for the construction of complex execution
 *     predicates by wrapping {@code atomic} conditions.
 * </p>
 * <b>Composition Example</b>
 *  A "Greater Than Or Equal to Zero" check can be composed by an{@code OrCondition}
 *  wrapping a {@code EqualsZeroCondition} and a {@code GreaterThanZeroCondition}.
 *
 *  @since 1.0.0
 *  @author TiJ
 */
package dk.tij.registermaschine.core.conditions;