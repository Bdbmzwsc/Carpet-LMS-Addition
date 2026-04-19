export interface BlockPos {
  x: number;
  y: number;
  z: number;
}

export interface PositionCount {
  pos: BlockPos;
  count: number;
}

export type PositionsByDimension = Record<string, PositionCount[]>;

export interface StorageItem {
  count: number;
  positionsByDimension: PositionsByDimension;
}

export interface StorageErrorPosition {
  dimension: string;
  pos: BlockPos;
}

export interface StorageData {
  items: Record<string, StorageItem>;
  errors: StorageErrorPosition[];
}

export interface StorageResponse {
  name: string;
  data: StorageData;
}
