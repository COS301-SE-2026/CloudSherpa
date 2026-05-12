// "use client";

// import React, {
//   useState,
//   useRef,
//   useCallback,
//   useEffect,
// } from "react";
// import { Card, CardHeader, CardTitle, CardContent } from "@/components/atoms/card";
// import { cn } from "@/lib/utils";


// //types
// export type WidgetSize = "2x2" | "2x4" | "4x4";

// export interface WidgetConfig {
//   id: string;
//   size: WidgetSize;
//   content: React.ReactNode;
//   title?: string;
// }

// export interface LayoutItem {
//   id: string;
//   col: number;
//   row: number;
//   size: WidgetSize;
// }

// export interface GridProps {
//   widgets: WidgetConfig[];
//   layout: LayoutItem[];
//   onLayoutChange?: (newLayout: LayoutItem[]) => void;
//   columns?: number;
//   className?: string;
// }


// //constants
// const SIZE_SPAN: Record<WidgetSize, { cols: number; rows: number }> = {
//   "2x2": { cols: 2, rows: 2 },
//   "2x4": { cols: 4, rows: 2 },
//   "4x4": { cols: 4, rows: 4 },
// };

// // how far (0-1) the ghost center must move into card before triggering swap
// // higher less sensitive/less jitter
// const SWAP_THRESHOLD = 0.35;


// //helpers
// function buildOrderedItems(widgets: WidgetConfig[], layout: LayoutItem[]) {
//   const layoutMap = new Map(layout.map((l) => [l.id, l]));
//   return widgets
//     .filter((w) => layoutMap.has(w.id))
//     .map((w) => ({ widget: w, layout: layoutMap.get(w.id)! }))
//     .sort((a, b) =>
//       a.layout.row !== b.layout.row
//         ? a.layout.row - b.layout.row
//         : a.layout.col - b.layout.col
//     );
// }

// function repackLayout(
//   orderedIds: string[],
//   widgets: WidgetConfig[],
//   totalCols: number
// ): LayoutItem[] {
//   const widgetMap = new Map(widgets.map((w) => [w.id, w]));
//   const grid: boolean[][] = [];

//   const occupy = (row: number, col: number, size: WidgetSize) => {
//     const { cols, rows } = SIZE_SPAN[size];
//     for (let r = row; r < row + rows; r++) {
//       if (!grid[r]) grid[r] = Array(totalCols).fill(false);
//       for (let c = col; c < col + cols; c++) grid[r][c] = true;
//     }
//   };

//   const findSlot = (size: WidgetSize) => {
//     const { cols, rows } = SIZE_SPAN[size];
//     for (let r = 0; ; r++) {
//       for (let c = 0; c <= totalCols - cols; c++) {
//         let fits = true;
//         for (let dr = 0; dr < rows && fits; dr++)
//           for (let dc = 0; dc < cols && fits; dc++)
//             if (grid[r + dr]?.[c + dc]) fits = false;
//         if (fits) return { row: r, col: c };
//       }
//     }
//   };

//   return orderedIds.reduce<LayoutItem[]>((acc, id) => {
//     const widget = widgetMap.get(id);
//     if (!widget) return acc;
//     const slot = findSlot(widget.size);
//     occupy(slot.row, slot.col, widget.size);
//     return [...acc, { id, col: slot.col + 1, row: slot.row + 1, size: widget.size }];
//   }, []);
// }

// function swapInto(ids: string[], fromIdx: number, toIdx: number): string[] {
//   const next = [...ids];
//   const [item] = next.splice(fromIdx, 1);
//   next.splice(toIdx, 0, item);
//   return next;
// }

// //flip helpers(furst,last,invert,play) apparently used fir performance focused animation
// //snapshots the bounds of every card
// function snapshotRects(
//   refs: Map<string, HTMLDivElement>
// ): Map<string, DOMRect> {
//   const map = new Map<string, DOMRect>();
//   refs.forEach((el, id) => map.set(id, el.getBoundingClientRect()));
//   return map;
// }


// function flipAnimate(
//   refs: Map<string, HTMLDivElement>,
//   before: Map<string, DOMRect>,
//   duration = 280
// ) {
//   refs.forEach((el, id) => {
//     const prev = before.get(id);
//     if (!prev) return;
//     const next = el.getBoundingClientRect();
//     const dx = prev.left - next.left;
//     const dy = prev.top - next.top;
//     if (Math.abs(dx) < 1 && Math.abs(dy) < 1) return;


//     el.style.transition = "none";
//     el.style.transform = `translate(${dx}px, ${dy}px)`;

//     el.getBoundingClientRect();

//     el.style.transition = `transform ${duration}ms cubic-bezier(0.25, 0.46, 0.45, 0.94)`;
//     el.style.transform = "";

//     const onEnd = () => {
//       el.style.transition = "";
//       el.style.transform = "";
//       el.removeEventListener("transitionend", onEnd);
//     };
//     el.addEventListener("transitionend", onEnd);
//   });
// }

// // grid
// export function Grid({
//   widgets,
//   layout,
//   onLayoutChange,
//   columns = 8,
//   className,
// }: GridProps) {
//   const orderedItems = buildOrderedItems(widgets, layout);

//   const [displayOrder, setDisplayOrder] = useState<string[]>(
//     () => orderedItems.map((i) => i.widget.id)
//   );

//   const [prevLayout, setPrevLayout] = useState(layout);
//   if (layout !== prevLayout) {
//     setPrevLayout(layout);
//     const newOrder = orderedItems.map((i) => i.widget.id);
//     // only reset if set of ids changed & not just reorder 
//     const same =
//       newOrder.length === displayOrder.length &&
//       newOrder.every((id) => displayOrder.includes(id));
//     if (!same) setDisplayOrder(newOrder);
//   }

//   const [draggingId, setDraggingId] = useState<string | null>(null);
//   const [ghostStyle, setGhostStyle] = useState<React.CSSProperties>({});

//   const cardRefs   = useRef<Map<string, HTMLDivElement>>(new Map());
//   const gridRef    = useRef<HTMLDivElement>(null);

//   const drag = useRef<{
//     id: string;
//     fromIdx: number;// current index in displayOrder
//     order: string[]; // live copy mutated without setState
//     startX: number;
//     startY: number;
//     width: number;
//     height: number;
//     originLeft: number;  
//     originTop: number;
//   } | null>(null);

//   //pointer down
//   const handlePointerDown = useCallback(
//     (e: React.PointerEvent, id: string) => {
//       if (e.button !== 0 && e.pointerType === "mouse") return;
//       e.currentTarget.setPointerCapture(e.pointerId);
//       e.preventDefault();

//       const el = cardRefs.current.get(id);
//       if (!el) return;
//       const rect = el.getBoundingClientRect();

//       drag.current = {
//         id,
//         fromIdx: displayOrder.indexOf(id),
//         order: [...displayOrder],
//         startX: e.clientX,
//         startY: e.clientY,
//         width: rect.width,
//         height: rect.height,
//         originLeft: rect.left,
//         originTop: rect.top,
//       };

//       setGhostStyle({
//         left: rect.left,
//         top: rect.top,
//         width: rect.width,
//         height: rect.height,
//       });
//       setDraggingId(id);
//     },
//     [displayOrder]
//   );

// // pointer move 
//   const handlePointerMove = useCallback((e: PointerEvent) => {
//     const ds = drag.current;
//     if (!ds) return;

//     const dx = e.clientX - ds.startX;
//     const dy = e.clientY - ds.startY;

//     // move ghost visuall
//     setGhostStyle((prev) => ({
//       ...prev,
//       left: ds.originLeft + dx,
//       top: ds.originTop + dy,
//     }));

//     const gcx = ds.originLeft + dx + ds.width / 2;
//     const gcy = ds.originTop + dy + ds.height / 2;

   
//     let bestId: string | null = null;
//     let bestScore = -1;

//     cardRefs.current.forEach((el, cardId) => {
//       if (cardId === ds.id) return;
//       const r = el.getBoundingClientRect();

//       // Wqhat fraction of the card's area does the ghost center sit within?
//       const inX = gcx >= r.left && gcx <= r.right;
//       const inY = gcy >= r.top  && gcy <= r.bottom;
//       if (!inX || !inY) return;

//       // score = how centred is ghost within this card (0–1 each axis)
//       const sx = 1 - Math.abs(gcx - (r.left + r.width  / 2)) / (r.width  / 2);
//       const sy = 1 - Math.abs(gcy - (r.top  + r.height / 2)) / (r.height / 2);
//       const score = sx * sy;

//       if (score > bestScore) {
//         bestScore = score;
//         bestId = cardId;
//       }
//     });

//     if (!bestId || bestScore < SWAP_THRESHOLD) return;

//     const toIdx = ds.order.indexOf(bestId);
//     if (toIdx === -1 || toIdx === ds.fromIdx) return;

//     const before = snapshotRects(cardRefs.current);

//     const newOrder = swapInto(ds.order, ds.fromIdx, toIdx);
//     ds.order    = newOrder;
//     ds.fromIdx  = toIdx;
//     setDisplayOrder([...newOrder]);

//     requestAnimationFrame(() => {
//       flipAnimate(cardRefs.current, before);
//     });
//   }, []);

// //pointer up
//   const handlePointerUp = useCallback(() => {
//     const ds = drag.current;
//     if (!ds) return;

//     onLayoutChange?.(repackLayout(ds.order, widgets, columns));
//     drag.current = null;
//     setDraggingId(null);
//   }, [widgets, columns, onLayoutChange]);

//   useEffect(() => {
//     window.addEventListener("pointermove", handlePointerMove);
//     window.addEventListener("pointerup",   handlePointerUp);
//     return () => {
//       window.removeEventListener("pointermove", handlePointerMove);
//       window.removeEventListener("pointerup",   handlePointerUp);
//     };
//   }, [handlePointerMove, handlePointerUp]);

//   //render
//   const itemMap = new Map(orderedItems.map((i) => [i.widget.id, i]));

//   const draggedItem = draggingId ? itemMap.get(draggingId) : null;

//   return (
//     <div
//       ref={gridRef}
//       className={cn("relative grid gap-4 auto-rows-[minmax(120px,auto)]", className)}
//       style={{ gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))` }}
//     >
//       {displayOrder.map((id) => {
//         const item = itemMap.get(id);
//         if (!item) return null;
//         const { widget, layout: li } = item;
//         const { cols, rows } = SIZE_SPAN[li.size];
//         const isDragging = draggingId === id;

//         return (
//           <Card
//             key={id}
//             ref={(el) => {
//               if (el) cardRefs.current.set(id, el);
//               else    cardRefs.current.delete(id);
//             }}
//             onPointerDown={(e) => handlePointerDown(e, id)}
//             style={{
//               gridColumn: `span ${cols}`,
//               gridRow:    `span ${rows}`,
//               willChange: "transform",
//             }}
//             className={cn(
//               "cursor-grab active:cursor-grabbing select-none touch-none",
//               isDragging && "opacity-0 pointer-events-none"
//             )}
//           >
//             {widget.title && (
//               <CardHeader className="pb-2">
//                 <CardTitle className="text-sm font-semibold">
//                   {widget.title}
//                 </CardTitle>
//               </CardHeader>
//             )}
//             <CardContent className={cn("flex-1", !widget.title && "pt-6")}>
//               {widget.content}
//             </CardContent>
//           </Card>
//         );
//       })}

//       {draggingId && draggedItem && (
//         <div
//           className="fixed z-50 pointer-events-none"
//           style={ghostStyle}
//         >
//           <Card
//             className={cn(
//               "w-full h-full",
//               "shadow-2xl ring-2 ring-primary/50",
//               "scale-[1.04] opacity-[0.97]",
//               "transition-shadow duration-150"
//             )}
//           >
//             {draggedItem.widget.title && (
//               <CardHeader className="pb-2">
//                 <CardTitle className="text-sm font-semibold">
//                   {draggedItem.widget.title}
//                 </CardTitle>
//               </CardHeader>
//             )}
//             <CardContent
//               className={cn("flex-1", !draggedItem.widget.title && "pt-6")}
//             >
//               {draggedItem.widget.content}
//             </CardContent>
//           </Card>
//         </div>
//       )}
//     </div>
//   );
// }

// export default Grid;