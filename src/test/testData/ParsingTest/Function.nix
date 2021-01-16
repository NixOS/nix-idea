[
(x: x)
(x: y: x + y)
({ x, y }: x + y)
({ x, y, ... }: x + y)
({ x ? "default" } : x)
(args@{ ... }: args)
({ ... } @ args: args)
]
