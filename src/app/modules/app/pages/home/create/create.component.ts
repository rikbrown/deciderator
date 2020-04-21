import {AfterViewInit, Component, Directive, ElementRef, TemplateRef, ViewChild, ViewChildren} from '@angular/core';
import {NgbModal, NgbModalRef} from '@ng-bootstrap/ng-bootstrap';
import {UncertaintyService} from '../../../../../core/services/uncertainty/uncertainty.service';
import {Router} from '@angular/router';
import {NewUncertainty} from '../../../../../core/services/uncertainty/types';

@Component({
  selector: 'app-create',
  templateUrl: './create.component.html',
  styleUrls: ['./create.component.scss'],
})
export class CreateComponent {
  uncertainty: NewUncertainty = null;
  submitted = false;
  failure?: string = null;

  @ViewChild('optionElement') someInput: ElementRef;
  @ViewChild('content') private contentTmpl: TemplateRef<any>;

  private modalRef: NgbModalRef;

  constructor(
    private uncertaintyService: UncertaintyService,
    private modalService: NgbModal,
    private router: Router,
  ) { }

  get filteredOptions(): Array<string> {
    return this.uncertainty.options
      .map((o) => o.name)
      .filter((o) => o.trim().length > 0)
      .filter((o, i, self) => self.indexOf(o) === i);
  }

  get optionsValid(): boolean {
    return this.filteredOptions.length >= 2;
  }

  open(): void {
    this.uncertainty = {
      name: '',
      options: [{ name: '' }, { name: '' }],
    };

    this.modalRef = this.modalService.open(this.contentTmpl, {
      centered: true,
    });
  }

  addChoice() {
    this.uncertainty.options.push({ name: '' });
  }

  onSubmit() {
    this.failure = null;
    this.submitted = true;
    this.uncertaintyService.createUncertainty(this.uncertainty).subscribe(
      (uncertaintyId) => {
        this.router.navigate(['/uncertainty', uncertaintyId]).then(() => this.modalRef?.close());
      },
      (err) => {
        this.submitted = false;
        this.failure = err.toString();
      }
    );
  }
}

interface Option {
  name: string;
}
