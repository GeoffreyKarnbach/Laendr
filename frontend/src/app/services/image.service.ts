import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Globals } from 'src/app/global';
import { Observable } from 'rxjs';
import { ImageCollection, ImageUpdatedCollection } from '../dtos';

@Injectable({
  providedIn: 'root',
})
export class ImageService {
  private imageBaseUri: string = this.globals.backendUri + '/images';

  constructor(private httpClient: HttpClient, private globals: Globals) {}

  /**
   * Upload images to the backend for a specific location
   *
   * @param File[] image files to persist in the backend
   * @param number locationId of the location to which the images belong
   */
  uploadImages(toUpload: File[], locationId: number): Observable<any> {
    console.log(toUpload);
    const formData = new FormData();

    for (const image of toUpload) {
      formData.append('images', image, image.name);
    }

    formData.append('locationId', locationId.toString());

    const header = new HttpHeaders();
    header.append('Content-Type', 'multipart/form-data');

    return this.httpClient.post(this.imageBaseUri, formData, { headers: header });
  }

  /**
   * Update images for a specific location
   *
   * @param toUploadUrl string[] of images to persist in the backend
   * @param locationID number of the location to which the images belong
   *
   * @returns Observable<any> containing the response from the backend, which is empty
   */
  updateImages(toUploadUrl: string[], locationID: number): Observable<any> {
    console.log(toUploadUrl);
    console.log(locationID);

    const toUpload = [];

    for (const image of toUploadUrl) {
      if (!image.startsWith('data:image')) {
        // Keep only content after last /
        const imageName = image.substring(image.lastIndexOf('/') + 1);
        toUpload.push(imageName);
      } else {
        toUpload.push(image);
      }
    }

    const content: ImageUpdatedCollection = {
      locationId: locationID,
      images: toUpload,
    };

    return this.httpClient.put(this.imageBaseUri, content);
  }

  /**
   * Returns all images for a specific location
   *
   * @param locationId number of the location to get the images for
   * @returns Observable<ImageCollection> containing all images for the location
   */
  getImages(locationId: number): Observable<ImageCollection> {
    return this.httpClient.get<ImageCollection>(this.imageBaseUri + '/all/' + locationId);
  }
}
